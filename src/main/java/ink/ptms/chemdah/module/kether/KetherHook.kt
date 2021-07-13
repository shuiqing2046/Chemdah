package ink.ptms.chemdah.module.kether

import io.izzel.taboolib.kotlin.kether.Kether
import io.izzel.taboolib.kotlin.kether.action.bukkit.PlayerOperator
import io.izzel.taboolib.module.inject.TFunction

object KetherHook {

    @TFunction.Init
    fun init() {
        Kether.addPlayerOperator("UUID", PlayerOperator(read = { it.uniqueId.toString() }))
    }
}