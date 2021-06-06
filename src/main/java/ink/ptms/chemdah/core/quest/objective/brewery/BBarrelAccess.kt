package ink.ptms.chemdah.core.quest.objective.brewery

import com.dre.brewery.api.events.barrel.BarrelAccessEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.brewery.BBarrelAccess
 *
 * @author Peng_Lx
 * @since 2021/6/4 23:02 下午
 */
@Dependency("Brewery")
object BBarrelAccess : ObjectiveCountableI<BarrelAccessEvent>() {

    override val name = "brewery barrel access"
    override val event = BarrelAccessEvent::class

    init {
        handler {
            player
        }
        addCondition("position") {
            toPosition().inside(it.clickedBlock.location)
        }
        addCondition("material") { e ->
            toInferBlock().isBlock(e.clickedBlock!!)
        }
        addCondition("face") { e ->
            asList().any { it.equals(e.clickedBlockFace.name, true) }
        }
    }
}