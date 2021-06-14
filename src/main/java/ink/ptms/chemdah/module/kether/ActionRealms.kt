package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.module.realms.RealmsSystem.getRealms
import ink.ptms.chemdah.util.ExpectDSL
import ink.ptms.chemdah.util.getPlayer
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionRealms
 *
 * @author sky
 * @since 2021/6/14 2:59 下午
 */
class ActionRealms {

    companion object {

        @KetherParser(["realms"], namespace = "chemdah")
        fun max() = ScriptParser.parser {
            ExpectDSL().actionNow {
                getPlayer().location.getRealms()?.id.toString()
            }
        }
    }
}