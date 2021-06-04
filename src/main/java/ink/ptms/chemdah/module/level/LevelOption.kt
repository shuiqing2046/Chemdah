package ink.ptms.chemdah.module.level

import ink.ptms.chemdah.util.asList
import io.izzel.taboolib.util.Coerce
import org.bukkit.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.module.level.LevelOption
 *
 * @author sky
 * @since 2021/4/15 4:58 下午
 */
class LevelOption(val algorithm: Level.Algorithm, val min: Int, val root: ConfigurationSection) {

    val id = root.name
    val reward = ArrayList<LevelReward>()

    init {
        root.getConfigurationSection("reward")?.getKeys(false)?.forEach { node ->
            val args = node.split("[-~]".toRegex())
            val range = (Coerce.toInteger(args[0])..Coerce.toInteger(args.getOrElse(1) { args[0] })).toList()
            reward.add(LevelReward(range, root.get("reward.$node")!!.asList()))
        }
    }

    fun toLevel(level: Int, experience: Int) = Level(algorithm, level.coerceAtLeast(min), experience)

    fun toLevel(playerLevel: PlayerLevel) = Level(algorithm, playerLevel.level.coerceAtLeast(min), playerLevel.experience)

    fun getReward(level: Int) = reward.firstOrNull { level in it.level }
}