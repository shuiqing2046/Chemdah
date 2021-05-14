package ink.ptms.chemdah.module.scenes

import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.inject.PlayerContainer
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.module.inject.TSchedule
import java.util.concurrent.ConcurrentHashMap

object ScenesSystem : Module {

    @TInject("module/scenes.yml")
    lateinit var conf: TConfig
        private set

    @PlayerContainer
    val automationMap = ConcurrentHashMap<String, MutableMap<String, Int>>()

    init {
        register()
    }

    override fun reload() {
        conf.reload()
    }

    @TSchedule(period = 20, async = true)
    fun e() {

    }
}