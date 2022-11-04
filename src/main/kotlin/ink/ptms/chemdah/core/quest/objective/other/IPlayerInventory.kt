package ink.ptms.chemdah.core.quest.objective.other

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import ink.ptms.chemdah.core.quest.objective.Progress
import ink.ptms.chemdah.core.quest.objective.Progress.Companion.toProgress
import org.bukkit.event.Event
import taboolib.platform.util.countItem
import taboolib.platform.util.hasItem
import taboolib.platform.util.takeItem

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.other.IPlayerInventory
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
object IPlayerInventory : ObjectiveCountableI<Event>() {

    override val name = "player inventory"
    override val event = Event::class.java
    override val isListener = false
    override val isTickable = true

    init {
        addFullCondition("item") { profile, task, _ ->
            val item = task.condition["item"]!!.toInferItem()
            val amount = task.condition["amount"]?.toInt() ?: 1
            val consume = task.condition["consume"]?.toBoolean() ?: false
            val inventory = profile.player.inventory
            val hasItem = inventory.hasItem(amount) { item.isItem(it) }
            if (hasItem && consume) {
                inventory.takeItem(amount) { item.isItem(it) }
            }
            hasItem
        }
    }

    override fun getProgress(profile: PlayerProfile, task: Task): Progress {
        val consume = task.condition["consume"]?.toBoolean() ?: false
        if (consume) {
            return super.getProgress(profile, task)
        }
        val item = task.condition["item"]!!.toInferItem()
        val amount = task.condition["amount"]?.toInt() ?: 1
        val value = profile.player.inventory.countItem { item.isItem(it) }.coerceAtMost(amount)
        return value.toProgress(amount)
    }
}