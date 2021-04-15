package ink.ptms.chemdah.module.level

import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.module.level.LevelOption
 *
 * @author sky
 * @since 2021/4/15 4:58 下午
 */
class LevelOption(val algorithm: Level.Algorithm, val min: Int, val root: ConfigurationSection) {

    fun toLevel(level: Int, experience: Int) = Level(algorithm, level.coerceAtLeast(min), experience)

    fun toLevel(playerLevel: PlayerLevel) = Level(algorithm, playerLevel.level.coerceAtLeast(min), playerLevel.experience)
}