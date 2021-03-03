package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.SingleListener
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.core.quest.objective.Objective
import io.izzel.taboolib.TabooLibLoader
import io.izzel.taboolib.compat.kotlin.CompatKotlin
import io.izzel.taboolib.module.inject.TFunction
import org.bukkit.entity.Player
import org.bukkit.event.Event

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.QuestManager
 *
 * @author sky
 * @since 2021/3/2 1:13 上午
 */
object QuestHandler {

    @Suppress("UNCHECKED_CAST")
    @TFunction.Init
    private fun register() {
        TabooLibLoader.getPluginClassSafely(Chemdah.plugin).forEach {
            if (Objective::class.java.isAssignableFrom(it)) {
                val objective = CompatKotlin.getInstance(it) as? Objective<Event>
                if (objective != null) {
                    ChemdahAPI.questObjective[objective.name] = objective
                    SingleListener.listen(objective.event.java, objective.priority, objective.ignoreCancelled) { e ->
                        objective.handler(e)?.run {
                            try {
                                handleEvent(this, e, objective)
                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } else if (it.isAnnotationPresent(Id::class.java)) {
                val id = it.getAnnotation(Id::class.java).id
                when {
                    Meta::class.java.isAssignableFrom(it) -> {
                        ChemdahAPI.questMeta[id] = it as Class<out Meta>
                    }
                    Addon::class.java.isAssignableFrom(it) -> {
                        ChemdahAPI.questAddon[id] = it as Class<out Addon>
                    }
                }
            }
        }
    }

    fun handleEvent(player: Player, event: Event, objective: Objective<Event>) {

    }
}