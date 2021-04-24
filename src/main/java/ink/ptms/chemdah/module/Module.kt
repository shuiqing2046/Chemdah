package ink.ptms.chemdah.module

import io.izzel.taboolib.module.inject.TFunction

/**
 * Chemdah
 * ink.ptms.chemdah.module.Module
 *
 * @author sky
 * @since 2021/3/23 5:11 下午
 */
interface Module {

    open fun reload() {
    }

    companion object {

        val modules = HashMap<String, Module>()

        fun Module.register() {
            modules[javaClass.simpleName] = this
        }

        @TFunction.Init
        fun reload() {
            modules.values.forEach { it.reload() }
        }
    }
}