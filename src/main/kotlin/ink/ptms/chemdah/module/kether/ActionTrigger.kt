package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.api.ChemdahAPI.callTrigger
import ink.ptms.chemdah.util.getBukkitPlayer
import taboolib.module.kether.*

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionTrigger
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
object ActionTrigger {

    /**
     * trigger def
     */
    @KetherParser(["trigger"], shared = true)
    fun parser() = scriptParser {
        val trigger = it.nextParsedAction()
        actionTake { run(trigger).str { t -> getBukkitPlayer().callTrigger(t) } }
    }
}