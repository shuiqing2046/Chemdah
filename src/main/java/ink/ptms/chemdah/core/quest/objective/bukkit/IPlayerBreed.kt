package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
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
object IPlayerBreed : ObjectiveCountableI<EntityBreedEvent>() {

    override val name = "entity breed"
    override val event = EntityBreedEvent::class

    init {
        handler {
            breeder as? Player
        }
        addCondition("position") {
            toPosition().inside(it.entity.location)
        }
        addCondition("entity") { 
            toInferEntity().isEntity(it.entity)
        }
        addCondition("entity:father") {
            toInferEntity().isEntity(it.father)
        }
        addCondition("entity:mother") {
            toInferEntity().isEntity(it.mother)
        }
        addCondition("item") { 
            toInferItem().isItem(it.bredWith ?: AIR)
        }
        addCondition("exp") { 
            toInt() < it.experience
        }
        addConditionVariable("exp") {
            it.experience
        }
    }
}