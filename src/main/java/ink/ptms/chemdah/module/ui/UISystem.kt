package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.module.inject.TSchedule
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.ConcurrentHashMap

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.UISystem
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
@TListener
object UISystem : Listener, Module {

    @TInject("module/ui.yml")
    lateinit var conf: TConfig
        private set

    val ui = ConcurrentHashMap<String, UI>()

    init {
        register()
    }

    @EventHandler
    private fun e(e: PlayerQuitEvent) {
        ui.values.forEach { it.playerFilters.remove(e.player.uniqueId) }
    }

    @TSchedule
    override fun reload() {
        ui.clear()
        ui.putAll(conf.getKeys(false).map { it to UI(conf.getConfigurationSection(it)!!) })
    }
}