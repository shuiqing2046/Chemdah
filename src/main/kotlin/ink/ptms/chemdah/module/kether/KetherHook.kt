package ink.ptms.chemdah.module.kether

import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.kether.Kether
import taboolib.module.kether.PlayerOperator
import taboolib.platform.compat.getBalance
import java.io.Reader

object KetherHook {

    @Awake(LifeCycle.INIT)
    fun init() {
        Kether.registeredPlayerOperator["balance"] = PlayerOperator(
            reader = PlayerOperator.Reader { it.cast<Player>().getBalance() },
            usable = emptyArray()
        )
    }
}