package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.conversation.ConversationManager.sessions
import ink.ptms.chemdah.util.namespaceConversationNPC
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.platform.function.warning
import taboolib.common5.Coerce
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.kether.KetherFunction
import taboolib.module.kether.KetherShell
import taboolib.module.kether.extend
import taboolib.module.kether.printKetherErrorMessage
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Chemdah
 * ink.ptms.chemdah.core.Conversation
 *
 * @author sky
 * @since 2021/2/9 6:18 下午
 */
data class Conversation(
    val id: String,
    val file: File?,
    val root: ConfigurationSection,
    var npcId: Trigger,
    val npcSide: MutableList<String>,
    val playerSide: PlayerSide,
    var condition: String?,
    val agent: MutableList<Agent>,
    val option: Option,
) {

    /**
     * 对话约束
     * 例如控制是否允许玩家跳过对话，或在对话中移动等
     */
    val flags = root.getStringList("flags").toMutableList().also {
        it.addAll(option.globalFlags)
    }

    /**
     * 对话转移
     */
    var transfer = ConversationTransfer(root.getConfigurationSection("transfer") ?: root.createSection("transfer"))

    fun hasFlag(value: String): Boolean {
        return value in flags
    }

    fun isNPC(namespace: String, id: String): Boolean {
        return npcId.id.any { it.isNPC(namespace, id) }
    }

    /**
     * 唤起不需要单位的对话
     *
     * @param player 玩家
     * @param name 对话名称
     */
    fun openSelf(player: Player, name: String): CompletableFuture<Session> {
        return open(player, object : Source<Any>(name, player) {

            override fun transfer(player: Player, newId: String): Boolean {
                return true
            }

            override fun getOriginLocation(entity: Any): Location {
                return player.eyeLocation.add(player.eyeLocation.direction)
            }
        })
    }

    /**
     * 唤起对话
     * 脚本代理的执行在添加对话内容之前
     * 所有脚本包括嵌入式在内都会继承会话中的所有变量
     *
     * @param player 玩家
     * @param source 来源
     * @param sessionTop 上层会话（继承关系）
     */
    fun <T> open(
        player: Player,
        source: Source<T>,
        sessionTop: Session? = null,
        consumer: Consumer<Session> = Consumer {},
    ): CompletableFuture<Session> {
        val future = CompletableFuture<Session>()
        val session = sessionTop ?: Session(this@Conversation, player.location.clone(), source.getOriginLocation(source.entity), player, source)
        // 事件
        if (!ConversationEvents.Pre(this@Conversation, session, sessionTop != null).call()) {
            future.complete(session)
            ConversationEvents.Cancelled(this@Conversation, session, true).call()
            return future
        }
        // 判定条件
        checkCondition(session).thenApply { condition ->
            if (condition) {
                // 会话转移
                if (transfer.id != null && !source.transfer(player, transfer.id!!)) {
                    warning("Unable to conversation transfer to $transfer (conversation: ${id}, player: ${player.name})")
                }
                // 注册会话
                sessions[player.name] = session
                consumer.accept(session)
                // 重置会话
                session.source = source
                session.reload()
                // 执行脚本代理
                agent(session, AgentType.BEGIN).thenApply {
                    // 重置会话展示
                    session.resetTheme().thenApply {
                        // 判断是否被脚本代理否取消对话
                        if (Coerce.toBoolean(session.variables["@Cancelled"])) {
                            // 仅关闭上层会话，只有会话开启才能被关闭
                            if (sessionTop != null) {
                                sessionTop.close().thenApply {
                                    future.complete(session)
                                    ConversationEvents.Cancelled(this@Conversation, session, true).call()
                                }
                            } else {
                                sessions.remove(player.name)
                                future.complete(session)
                                ConversationEvents.Cancelled(this@Conversation, session, false).call()
                            }
                        } else {
                            // 添加对话内容
                            session.npcSide.addAll(npcSide.map {
                                try {
                                    KetherFunction.parse(it, namespace = namespaceConversationNPC) {
                                        extend(session.variables)
                                    }
                                } catch (e: Throwable) {
                                    e.printKetherErrorMessage()
                                    e.localizedMessage
                                }
                            })
                            ConversationEvents.Begin(this@Conversation, session, sessionTop != null).call()
                            // 渲染对话
                            option.instanceTheme.onBegin(session).thenAccept {
                                // 脚本代理
                                agent(session, AgentType.START).thenAccept {
                                    future.complete(session)
                                    ConversationEvents.Post(this@Conversation, session, sessionTop != null).call()
                                }
                            }
                        }
                    }
                }
            } else {
                sessions.remove(player.name)
                future.complete(session)
                ConversationEvents.Cancelled(this@Conversation, session, false).call()
            }
        }
        return future
    }

    fun checkCondition(session: Session): CompletableFuture<Boolean> {
        return CompletableFuture<Boolean>().also { future ->
            if (condition == null) {
                future.complete(true)
                return@also
            }
            try {
                KetherShell.eval(condition!!, namespace = namespaceConversationNPC) { extend(session.variables) }.thenApply {
                    future.complete(Coerce.toBoolean(it))
                }
            } catch (e: Throwable) {
                future.complete(false)
                e.printKetherErrorMessage()
            }
        }
    }

    /**
     * 指定脚本代理
     * 会在运行结束后将所有变量覆盖写入会话
     *
     * @param session 会话实例
     * @param type 脚本代理类型
     */
    fun agent(session: Session, type: AgentType): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        if (!ConversationEvents.Agent(this@Conversation, session, type).call()) {
            future.complete(null)
            return future
        }
        val agents = agent.filter { it.type == type }
        fun process(cur: Int) {
            if (cur < agents.size) {
                try {
                    val agent = agents[cur].action.toMutableList().also { it.add("agent") }
                    KetherShell.eval(agent, namespace = type.namespaceAll()) {
                        extend(session.variables)
                        rootFrame().variables()["type"] = type.name
                        rootFrame().variables()["@Session"] = session
                    }.thenApply {
                        if (Coerce.toBoolean(session.variables["@Cancelled"])) {
                            future.complete(null)
                        } else {
                            process(cur + 1)
                        }
                    }
                } catch (e: Throwable) {
                    session.close()
                    e.printKetherErrorMessage()
                }
            } else {
                future.complete(null)
            }
        }
        process(0)
        return future
    }
}