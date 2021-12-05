package ink.ptms.chemdah.core.quest.objective.bukkit.paper

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerCriterionGrant
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerCriterionGrant : ObjectiveCountableI<PlayerAdvancementCriterionGrantEvent>() {

    override val name = "criterion grant"
    override val event = PlayerAdvancementCriterionGrantEvent::class

    init {
        handler {
            player
        }
        addSimpleCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addSimpleCondition("advancement") { e ->
            asList().any { it.equals(e.advancement.key.toString(), true) }
        }
        addSimpleCondition("criterion") { e ->
            asList().any { it.equals(e.criterion, true) }
        }
    }
}