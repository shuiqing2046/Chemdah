package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityResurrectEvent
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.getUsingItem

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerResurrect
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerResurrect : ObjectiveCountableI<EntityResurrectEvent>() {

    override val name = "player resurrect"
    override val event = EntityResurrectEvent::class.java

    val totem by lazy { XMaterial.TOTEM_OF_UNDYING.parseMaterial()!! }

    init {
        handler {
            it.entity as? Player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.entity.location)
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem((e.entity as Player).getUsingItem(totem) ?: return@addSimpleCondition false)
        }
    }
}