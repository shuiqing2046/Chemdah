package ink.ptms.chemdah.module.ui

import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import java.util.concurrent.ConcurrentHashMap

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.UISystem
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
@Awake
object UISystem : Module {

    @Config("module/ui.yml")
    lateinit var conf: Configuration
        private set

    val ui = ConcurrentHashMap<String, UI>()

    init {
        register()
    }

    fun getUI(name: String) = ui[name]

    @SubscribeEvent
    internal fun onReleased(e: PlayerEvents.Released) {
        ui.values.forEach { it.playerFilters.remove(e.player.uniqueId) }
    }

    override fun reload() {
        conf.reload()
        ui.clear()
        ui.putAll(conf.getKeys(false).map { it to UI(conf.getConfigurationSection(it)!!) })
    }
}