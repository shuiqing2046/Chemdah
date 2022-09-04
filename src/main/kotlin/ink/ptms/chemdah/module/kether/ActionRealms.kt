package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.module.realms.RealmsSystem.getRealms
import ink.ptms.chemdah.util.getBukkitPlayer
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
internal object ActionRealms {

    @KetherParser(["realms"], namespace = "chemdah", shared = true)
    fun realms() = scriptParser {
        actionNow { getBukkitPlayer().location.getRealms()?.id.toString() }
    }
}