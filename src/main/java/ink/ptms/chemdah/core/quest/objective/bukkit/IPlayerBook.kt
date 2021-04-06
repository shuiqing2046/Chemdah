package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountable
import org.bukkit.event.player.PlayerEditBookEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerBook
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerBook : ObjectiveCountable<PlayerEditBookEvent>() {

    override val name = "edit book"
    override val event = PlayerEditBookEvent::class

    init {
        handler {
            player
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("position") || task.condition["position"]!!.toPosition().inside(e.player.location)
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("signing") || task.condition["signing"]!!.toBoolean() == e.isSigning
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("page") || task.condition["page"]!!.toInt() <= e.newBookMeta.pageCount
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("title") || task.condition["title"]!!.toString() in e.newBookMeta.title.toString()
        }
        addCondition { _, task, e ->
            !task.condition.containsKey("content") || task.condition["content"]!!.toString() in e.newBookMeta.pages.toString()
        }
    }
}