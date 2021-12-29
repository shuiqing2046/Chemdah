package ink.ptms.chemdah.core.quest.objective.brewery

import com.dre.brewery.api.events.PlayerPushEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.brewery.BPush
 *
 * @author Peng_Lx
 * @since 2021/6/4 23:02 下午
 */
@Dependency("Brewery")
object BPush : ObjectiveCountableI<PlayerPushEvent>() {

    override val name = "brewery push"
    override val event = PlayerPushEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.player.location)
        }
        addSimpleCondition("x") { data, it ->
            data.toInt() <= it.push.x
        }
        addSimpleCondition("y") { data, it ->
            data.toInt() <= it.push.y
        }
        addSimpleCondition("z") { data, it ->
            data.toInt() <= it.push.z
        }
        addConditionVariable("x") {
            it.push.x
        }
        addConditionVariable("y") {
            it.push.y
        }
        addConditionVariable("z") {
            it.push.z
        }
    }
}