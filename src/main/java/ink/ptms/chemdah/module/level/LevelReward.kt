package ink.ptms.chemdah.module.level

import ink.ptms.chemdah.util.print
import io.izzel.taboolib.kotlin.kether.KetherShell
import org.bukkit.entity.Player

/**
 * Chemdah
 * ink.ptms.chemdah.module.level.LevelReward
 *
 * @author sky
 * @since 2021/4/17 12:45 下午
 */
class LevelReward(val level: List<Int>, val script: List<String>) {

    fun eval(player: Player, level: Int) {
        try {
            KetherShell.eval(script) {
                sender = player
                rootFrame().variables().set("level", level)
            }
        } catch (ex: Exception) {
            ex.print()
        }
    }
}