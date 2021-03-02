package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.core.quest.objective.Objective
import io.izzel.taboolib.TabooLibLoader
import io.izzel.taboolib.compat.kotlin.CompatKotlin
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.module.inject.TInjectHelper

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.QuestManager
 *
 * @author sky
 * @since 2021/3/2 1:13 上午
 */
object QuestManager {

    @Suppress("UNCHECKED_CAST")
    @TFunction.Init
    private fun register() {
        TabooLibLoader.getPluginClassSafely(Chemdah.plugin).forEach {
            if (Objective::class.java.isAssignableFrom(it)) {
                val objective = CompatKotlin.getInstance(it) as? Objective
                if (objective != null) {
                    ChemdahAPI.questObjective[objective.name] = objective
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
}