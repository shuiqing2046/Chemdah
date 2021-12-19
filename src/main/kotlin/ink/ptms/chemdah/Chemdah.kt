package ink.ptms.chemdah

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.console
import taboolib.common.platform.function.disablePlugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.kether.Kether
import taboolib.module.lang.sendLang
import taboolib.module.nms.MinecraftVersion
import taboolib.platform.BukkitPlugin

object Chemdah : Plugin() {

    @Config
    lateinit var conf: Configuration
        private set

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    override fun onLoad() {
        if (MinecraftVersion.majorLegacy < 10900 || !MinecraftVersion.isSupported) {
            console().sendLang("not-support")
            disablePlugin()
        }
    }

    override fun onEnable() {
        Kether.isAllowToleranceParser = true
    }
}
