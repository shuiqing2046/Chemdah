package ink.ptms.chemdah.module.scenes

import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import taboolib.common.platform.Awake
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile

@Awake
object ScenesSystem : Module {

    @Config("module/scenes.yml")
    lateinit var conf: SecuredFile
        private set

    val scenesMap = HashMap<String, ScenesFile>()

    init {
        register()
    }

    override fun reload() {
        scenesMap.clear()
        conf.reload()
        conf.getKeys(false).forEach {
            scenesMap[it] = ScenesFile(conf.getConfigurationSection(it)!!)
        }
    }
}