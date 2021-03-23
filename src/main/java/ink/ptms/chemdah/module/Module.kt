package ink.ptms.chemdah.module

/**
 * Chemdah
 * ink.ptms.chemdah.module.Module
 *
 * @author sky
 * @since 2021/3/23 5:11 下午
 */
interface Module {

    fun reload()

    companion object {

        val modules = HashMap<String, Module>()

        fun Module.register() {
            modules[javaClass.simpleName] = this
        }
    }
}