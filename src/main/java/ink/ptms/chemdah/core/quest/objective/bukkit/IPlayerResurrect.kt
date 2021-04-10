package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import io.izzel.taboolib.cronus.CronusUtils
import io.izzel.taboolib.internal.xseries.XMaterial
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityResurrectEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerResurrect
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerResurrect : ObjectiveCountable<EntityResurrectEvent>() {

    override val name = "player resurrect"
    override val event = EntityResurrectEvent::class

    val totem = XMaterial.TOTEM_OF_UNDYING.parseMaterial()

    init {
        handler {
            entity as? Player
        }
        addCondition("position") { e ->
            toPosition().inside(e.entity.location)
        }
        addCondition("item") { e ->
            toInferItem().isItem(CronusUtils.getUsingItem(e.entity as Player, totem))
        }
    }
}