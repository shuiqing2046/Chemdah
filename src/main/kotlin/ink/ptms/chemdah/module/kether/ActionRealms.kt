package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.module.realms.RealmsSystem.getRealms
import ink.ptms.chemdah.util.getPlayer
import taboolib.module.kether.KetherParser
import taboolib.module.kether.actionNow
import taboolib.module.kether.scriptParser

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionRealms
 *
 * @author sky
 * @since 2021/6/14 2:59 下午
 */
class ActionRealms {

    companion object {

        @KetherParser(["realms"], namespace = "chemdah", shared = true)
        fun max() = scriptParser {
            actionNow {
                getPlayer().location.getRealms()?.id.toString()
            }
        }
    }
}