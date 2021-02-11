package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.core.conversation.ConversationManager.sessions
import ink.ptms.chemdah.core.script.extend
import ink.ptms.chemdah.core.script.namespace
import ink.ptms.chemdah.core.script.namespaceConversationNPC
import ink.ptms.chemdah.core.script.print
import io.izzel.taboolib.kotlin.kether.KetherFunction
import io.izzel.taboolib.kotlin.kether.KetherShell
import io.izzel.taboolib.kotlin.kether.common.util.LocalizedException
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
    val npcId: Trigger,
    val npcSide: List<String>,
    val playerSide: PlayerSide,
    val condition: String?,
    val agent: List<Agent>,
    val option: Option,
) {

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
    fun open(player: Player, origin: Location, sessionTop: Session? = null): CompletableFuture<Session> {
        val future = CompletableFuture<Session>()
        val session = sessionTop ?: Session(this, player.location.clone(), origin.clone(), player)
        // 注册会话
        sessions[player.name] = session
        // 重置会话
        session.reload()
        // 执行脚本代理
        agent(session, AgentType.BEGIN).thenApply {
            // 重置会话展示
            session.reloadTheme().thenApply {
                // 判断是否被脚本代理否取消对话
                if (Coerce.toBoolean(session.variables["@Cancelled"])) {
                    // 仅关闭上层会话，只有会话开启才能被关闭
                    if (sessionTop != null) {
                        sessionTop.close().thenApply {
                            future.complete(session)
                        }
                    } else {
                        future.complete(session)
                    }
                } else {
                    // 添加对话内容
                    session.npcSide.addAll(npcSide.map {
                        try {
                            KetherFunction.parse(it, namespace = namespaceConversationNPC) {
                                extend(session.variables)
                            }
                        } catch (e: LocalizedException) {
                            e.print()
                            e.localizedMessage
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            e.localizedMessage
                        }
                    })
                    // 渲染对话
                    option.instanceTheme.begin(session).thenAccept {
                        future.complete(session)
                    }
                }
            }
        }
        return future
    }

    /**
     * 指定脚本代理
     * 会在运行结束后将所有变量覆盖写入会话
     *
     * @param session 会话实例
     * @param agentType 脚本代理类型
     */
    fun agent(session: Session, agentType: AgentType): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        val agents = agent.filter { it.type == agentType }
        fun process(cur: Int) {
            if (cur < agents.size) {
                try {
                    KetherShell.eval(agents[cur].action.toMutableList().also {
                        it.add("agent")
                    }, namespace = agentType.namespace()) {
                        extend(session.variables)
                    }.thenApply {
                        process(cur + 1)
                    }
                } catch (e: LocalizedException) {
                    e.print()
                    session.close()
                } catch (e: Throwable) {
                    e.printStackTrace()
                    session.close()
                }
            } else {
                future.complete(null)
            }
        }
        process(0)
        return future
    }
}