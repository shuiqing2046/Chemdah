package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.conversation.ConversationManager.sessions
import ink.ptms.chemdah.util.*
import io.izzel.taboolib.kotlin.kether.KetherFunction
import io.izzel.taboolib.kotlin.kether.KetherShell
import io.izzel.taboolib.util.Coerce
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.io.File
import java.util.concurrent.CompletableFuture

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
    val flags = root.getStringList("flags").toMutableList()

    fun hasFlag(value: String): Boolean {
        return value in flags
    }

    fun isNPC(namespace: String, id: String): Boolean {
        return npcId.id.any { it.namespace.equals(namespace, true) && it.value == id }
    }

    /**
     * 唤起对话
     * 脚本代理的执行在添加对话内容之前
     * 所有脚本包括嵌入式在内都会继承会话中的所有变量
     *
     * @param player 玩家
     * @param origin 原点（对话实体的头顶坐标）
     * @param sessionTop 上层会话（继承关系）
     */
    fun open(
        player: Player,
        origin: Location,
        sessionTop: Session? = null,
        npcName: String? = null,
        npcObject: Any? = null,
        consumer: (Session) -> Unit = {}
    ): CompletableFuture<Session> {
        val future = CompletableFuture<Session>()
        mirrorFuture("Conversation:open") {
            val session = sessionTop ?: Session(this@Conversation, player.location.clone(), origin.clone(), player)
            // 事件
            if (ConversationEvents.Pre(this@Conversation, session, sessionTop != null).call().isCancelled) {
                future.complete(session)
                finish()
                ConversationEvents.Cancelled(this@Conversation, session, true).call()
                return@mirrorFuture
            }
            if (npcName != null) {
                session.npcName = npcName
                session.npcObject = npcObject
            }
            // 注册会话
            sessions[player.name] = session
            consumer(session)
            // 重置会话
            session.reload()
            // 判定条件
            checkCondition(session).thenApply { condition ->
                if (condition) {
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
                                        e.print()
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
                            finish()
                        }
                    }
                } else {
                    sessions.remove(player.name)
                    future.complete(session)
                    finish()
                    ConversationEvents.Cancelled(this@Conversation, session, false).call()
                }
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
                KetherShell.eval(condition!!, namespace = namespaceConversationNPC) {
                    extend(session.variables)
                }.thenApply {
                    future.complete(Coerce.toBoolean(it))
                }
            } catch (e: Throwable) {
                future.complete(false)
                e.print()
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
        mirrorFuture("Conversation:agent") {
            if (ConversationEvents.Agent(this@Conversation, session, type).call().isCancelled) {
                future.complete(null)
                finish()
            }
            val agents = agent.filter { it.type == type }
            fun process(cur: Int) {
                if (cur < agents.size) {
                    try {
                        KetherShell.eval(agents[cur].action.toMutableList().also {
                            it.add("agent")
                        }, namespace = type.namespaceAll()) {
                            extend(session.variables)
                        }.thenApply {
                            if (Coerce.toBoolean(session.variables["@Cancelled"])) {
                                future.complete(null)
                            } else {
                                process(cur + 1)
                            }
                        }
                    } catch (e: Throwable) {
                        session.close()
                        e.print()
                    }
                } else {
                    future.complete(null)
                }
            }
            process(0)
            finish()
        }
        return future
    }
}