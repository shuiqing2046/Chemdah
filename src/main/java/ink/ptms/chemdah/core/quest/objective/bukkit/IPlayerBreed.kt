package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityBreedEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerBreed
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerBreed : ObjectiveCountable<EntityBreedEvent>() {

    override val name = "entity breed"
    override val event = EntityBreedEvent::class

    init {
        handler {
            if (breeder is Player) breeder as Player else null
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.entity.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("entity") || task.condition["entity"]!!.toInferEntity().isEntity(e.entity)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("entity:father") || task.condition["entity:father"]!!.toInferEntity().isEntity(e.father)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("entity:mother") || task.condition["entity:mother"]!!.toInferEntity().isEntity(e.mother)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("item") || task.condition["item"]!!.toInferItem().isItem(e.bredWith ?: AIR)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("exp") || task.condition["exp"]!!.toInt() < e.experience
        }
    }
}