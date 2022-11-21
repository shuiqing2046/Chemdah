package ink.ptms.chemdah.core.conversation

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.util.flatLines
import ink.ptms.chemdah.util.namespaceConversationPlayer
import org.bukkit.entity.Player
import taboolib.common.util.asList
import taboolib.common5.Coerce
import taboolib.module.chat.colored
import taboolib.module.kether.KetherFunction
import taboolib.module.kether.KetherShell
import taboolib.module.kether.extend
import taboolib.module.kether.printKetherErrorMessage
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.Reply
 *
 * @author sky
 * @since 2021/2/9 6:23 下午
 */
data class PlayerReply(val root: MutableMap<String, Any?>) {

    /** 回复条件 **/
    var condition = root["if"]?.toString()

    /** 回复文本 **/
    var text = root["reply"].toString()

    /** 回复格式 **/
    var format = root["format"]?.toString() ?: root["type"]?.toString()

    /** 回复动作 **/
    val action = root["then"]?.asList()?.flatLines()?.toMutableList() ?: arrayListOf() // 兼容 Chemdah Lab

    /** 换行 **/
    val swapLine = Coerce.toBoolean(root["swap"])

    /** 唯一选择 **/
    val uniqueId = root["unique"]?.toString()

    /** 回复 ID **/
    val rid: UUID = UUID.randomUUID()

    /**
     * 玩家是否选择过该回复
     */
    fun isPlayerSelected(player: Player): Boolean {
        if (uniqueId != null && player.isChemdahProfileLoaded) {
            return player.chemdahProfile.persistentDataContainer.containsKey("conversation.unique.$uniqueId")
        }
        return false
    }

    /**
     * 构建回复内容
     */
    fun build(session: Session): String {
        return try {
            KetherFunction.parse(text, namespace = namespaceConversationPlayer) { extend(session.variables) }.colored()
        } catch (e: Throwable) {
            e.printKetherErrorMessage()
            e.localizedMessage
        }
    }

    /**
     * 检查回复是否可以使用
     */
    fun check(session: Session): CompletableFuture<Boolean> {
        return when {
            // 已选择
            session.isSelected -> {
                CompletableFuture.completedFuture(false)
            }
            // 无条件
            condition == null -> {
                CompletableFuture.completedFuture(true)
            }

            else -> {
                try {
                    KetherShell.eval(condition!!, namespace = namespaceConversationPlayer) { extend(session.variables) }.thenApply {
                        Coerce.toBoolean(it)
                    }
                } catch (e: Throwable) {
                    e.printKetherErrorMessage()
                    CompletableFuture.completedFuture(false)
                }
            }
        }
    }

    /**
     * 选择该回复项
     */
    fun select(session: Session): CompletableFuture<Void> {
        // 已选择
        if (session.isSelected) {
            return CompletableFuture.completedFuture(null)
        }
        // 事件
        if (!ConversationEvents.SelectReply(session.player, session, this).call()) {
            return CompletableFuture.completedFuture(null)
        }
        // 记录选择
        if (uniqueId != null && session.player.isChemdahProfileLoaded) {
            session.player.chemdahProfile.persistentDataContainer["conversation.unique.$uniqueId"] = true
        }
        session.isSelected = true
        return try {
            KetherShell.eval(action, namespace = namespaceConversationPlayer) { extend(session.variables) }.thenAccept {
                if (session.isNext) {
                    session.isNext = false
                } else {
                    ConversationEvents.ReplyClosed(session).call()
                    session.close()
                }
            }
        } catch (e: Throwable) {
            e.printKetherErrorMessage()
            session.close()
            CompletableFuture.completedFuture(null)
        }
    }
}