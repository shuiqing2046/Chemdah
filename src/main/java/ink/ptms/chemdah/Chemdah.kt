package ink.ptms.chemdah

import io.izzel.taboolib.loader.Plugin
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.inject.TInject

object Chemdah : Plugin() {

    @TInject(migrate = true)
    lateinit var conf: TConfig
        private set

    override fun allowHotswap() = false
}
