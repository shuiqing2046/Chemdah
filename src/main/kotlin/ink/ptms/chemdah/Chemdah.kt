package ink.ptms.chemdah

import org.bukkit.Bukkit
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.disablePlugin
import taboolib.common.platform.function.warning
import taboolib.common.util.Version
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile

object Chemdah : Plugin() {

    @Config
    lateinit var conf: SecuredFile
        private set

    override fun onLoad() {
        if (Version(Bukkit.getPluginManager().getPlugin("Adyeshach")!!.description.version) < Version("1.3.13")) {
            warning("Adyeshach < 1.3.13")
            disablePlugin()
        }
    }
}
