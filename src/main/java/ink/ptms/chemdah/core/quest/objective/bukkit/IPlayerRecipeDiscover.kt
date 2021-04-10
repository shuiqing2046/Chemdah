package ink.ptms.chemdah.core.quest.objective.bukkit

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.event.player.PlayerRecipeDiscoverEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerRecipeDiscover
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerRecipeDiscover : ObjectiveCountable<PlayerRecipeDiscoverEvent>() {

    override val name = "recipe discover"
    override val event = PlayerRecipeDiscoverEvent::class

    init {
        handler {
            player
        }
        addCondition("position") { e ->
            toPosition().inside(e.player.location)
        }
        addCondition("recipe") { e ->
            asList().any { it.equals(e.recipe.toString(), true) }
        }
    }
}