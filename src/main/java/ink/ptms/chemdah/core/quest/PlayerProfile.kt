package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.core.Metadata
import ink.ptms.chemdah.core.script.namespaceQuest
import ink.ptms.chemdah.core.script.print
import ink.ptms.chemdah.util.asList
import io.izzel.taboolib.internal.gson.annotations.Expose
import io.izzel.taboolib.kotlin.kether.KetherShell
import io.izzel.taboolib.util.Coerce
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.PlayerProfile
 *
 * @author sky
 * @since 2021/3/2 12:00 上午
 */
class PlayerProfile(val uniqueId: UUID) {

    val player: Player
        get() = Bukkit.getPlayer(uniqueId)!!

    val metadata = Metadata()
    val quest = ConcurrentHashMap<String, Quest>()

    fun <T> metadata(task: Task, func: QuestMetaOperator.() -> T): T {
        return func.invoke(QuestMetaOperator(this, task))
    }

    fun checkAgent(agent: Any?, event: Event? = null): CompletableFuture<Boolean> {
        agent ?: return CompletableFuture.completedFuture(true)
        return try {
            KetherShell.eval(agent.asList(), namespace = namespaceQuest) {
                this.sender = player
                this.event = event
            }.thenApply {
                Coerce.toBoolean(it)
            }
        } catch (e: Throwable) {
            e.print()
            CompletableFuture.completedFuture(false)
        }
    }
}