package ink.ptms.chemdah.core.quest.objective.sandalphon

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import ink.ptms.sandalphon.module.impl.treasurechest.event.ChestOpenEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.sandalphon.SChestOpen
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("Sandalphon")
object SChestOpen : ObjectiveCountableI<ChestOpenEvent>() {

    override val name = "sandalphon chest open"
    override val event = ChestOpenEvent::class

    init {
        handler {
            player
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.chestData.block)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("title") || task.condition["title"]!!.toString().equals(e.chestData.title, true)
        }
    }
}