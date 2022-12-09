package ink.ptms.chemdah.module

import ink.ptms.chemdah.api.event.collect.PluginReloadEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

/**
 * Chemdah
 * ink.ptms.chemdah.module.Module
 *
 * @author sky
 * @since 2021/3/23 5:11 下午
 */
interface Module {

    fun reload() {
    }

    companion object {

        val modules = HashMap<String, Module>()

        fun Module.register() {
            modules[javaClass.simpleName] = this
        }

        @Awake(LifeCycle.ENABLE)
        fun reload() {
            modules.values.forEach { it.reload() }
            PluginReloadEvent.Module().call()
        }
    }
}