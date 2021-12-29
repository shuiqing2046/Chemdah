package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerRecipeDiscoverEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerRecipeDiscover
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerRecipeDiscover : ObjectiveCountableI<PlayerRecipeDiscoverEvent>() {

    override val name = "recipe discover"
    override val event = PlayerRecipeDiscoverEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("recipe") { data, e ->
            data.asList().any { it.equals(e.recipe.toString(), true) }
        }
    }
}