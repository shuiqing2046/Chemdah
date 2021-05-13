package ink.ptms.chemdah.module.scenes

import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.inject.TInject

object ScenesSystem : Module {

    @TInject("module/scenes.yml")
    lateinit var conf: TConfig
        private set

    init {
        register()
    }

    override fun reload() {
        conf.reload()
    }
}