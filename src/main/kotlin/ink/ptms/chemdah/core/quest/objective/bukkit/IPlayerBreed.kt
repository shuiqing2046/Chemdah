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
            it.breeder as? Player
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.entity.location)
        }
        addSimpleCondition("entity") { data, it ->
            data.toInferEntity().isEntity(it.entity)
        }
        addSimpleCondition("entity:father") { data, it ->
            data.toInferEntity().isEntity(it.father)
        }
        addSimpleCondition("entity:mother") { data, it ->
            data.toInferEntity().isEntity(it.mother)
        }
        addSimpleCondition("item") { data, it ->
            data.toInferItem().isItem(it.bredWith ?: EMPTY_ITEM)
        }
        addSimpleCondition("exp") { data, it ->
            data.toInt() <= it.experience
        }
        addConditionVariable("exp") {
            it.experience
        }
    }
}