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
    override val event = EntityBreedEvent::class.java

    init {
        handler {
            breeder as? Player
        }
        addSimpleCondition("position") {
            toPosition().inside(it.entity.location)
        }
        addSimpleCondition("entity") {
            toInferEntity().isEntity(it.entity)
        }
        addSimpleCondition("entity:father") {
            toInferEntity().isEntity(it.father)
        }
        addSimpleCondition("entity:mother") {
            toInferEntity().isEntity(it.mother)
        }
        addSimpleCondition("item") {
            toInferItem().isItem(it.bredWith ?: AIR)
        }
        addSimpleCondition("exp") {
            toInt() < it.experience
        }
        addConditionVariable("exp") {
            it.experience
        }
    }
}