package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Baffle
import taboolib.platform.util.isMovement
import java.util.concurrent.TimeUnit

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerMove
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerMove : ObjectiveCountableI<PlayerMoveEvent>() {

    override val name = "player move"
    override val event = PlayerMoveEvent::class.java

    val lock = Baffle.of(200, TimeUnit.MILLISECONDS)

    init {
        handler {
            if (it.isMovement() && lock.hasNext(it.player.name)) it.player else null
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.to!!)
        }
        addSimpleCondition("position:to") { data, e ->
            data.toPosition().inside(e.to!!)
        }
        addSimpleCondition("position:from") { data, e ->
            data.toPosition().inside(e.from)
        }
    }

    @SubscribeEvent
    private fun onQuit(e: PlayerQuitEvent) {
        lock.reset(e.player.name)
    }
}