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
    override val event = BarrelAccessEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.clickedBlock.location)
        }
        addSimpleCondition("material") { data, e ->
            data.toInferBlock().isBlock(e.clickedBlock!!)
        }
        addSimpleCondition("face") { data, e ->
            data.asList().any { it.equals(e.clickedBlockFace.name, true) }
        }
    }
}