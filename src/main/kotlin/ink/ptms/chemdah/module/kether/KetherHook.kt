package ink.ptms.chemdah.module.kether

import ink.ptms.adyeshach.taboolib.common.LifeCycle
import ink.ptms.adyeshach.taboolib.common.platform.Awake
import taboolib.module.kether.Kether
import taboolib.module.kether.PlayerOperator

object KetherHook {

    @Awake(LifeCycle.INIT)
    fun init() {
        Kether.registeredPlayerOperator["UUID"] = PlayerOperator(
            reader = PlayerOperator.Reader { it.uniqueId.toString() },
            usable = emptyArray()
        )
    }
}