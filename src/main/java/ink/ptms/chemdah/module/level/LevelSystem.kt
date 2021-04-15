package ink.ptms.chemdah.module.level

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import ink.ptms.chemdah.util.warning
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.Features
import javax.script.SimpleBindings

/**
 * Chemdah
 * ink.ptms.chemdah.module.level.LevelSystem
 *
 * @author sky
 * @since 2021/3/8 11:13 下午
 */
object LevelSystem : Module {

    @TInject("module/level.yml")
    lateinit var conf: TConfig
        private set

    val level = HashMap<String, LevelOption>()

    init {
        register()
    }

    fun PlayerProfile.setLevel(node: String, playerLevel: PlayerLevel) {
        setLevel(node, playerLevel.level, playerLevel.experience)
    }

    fun PlayerProfile.setLevel(node: String, level: Int, experience: Int) {
        persistentDataContainer["module.level.$node.level"] = level
        persistentDataContainer["module.level.$node.experience"] = experience
    }

    fun PlayerProfile.getLevel(node: String): PlayerLevel {
        return PlayerLevel(
            persistentDataContainer["module.level.$node.level", 0].toInt(),
            persistentDataContainer["module.level.$node.experience", 0].toInt()
        )
    }

    fun getLevel(name: String) = level[name]

    override fun reload() {
        level.clear()
        conf.getKeys(false).forEach { node ->
            val section = conf.getConfigurationSection(node)!!
            val algorithm = when (section.getString("experience.type")) {
                "math" -> {
                    val script = Features.compileScript(section.getString("experience.math")) ?: return@forEach
                    object : Level.Algorithm() {

                        override val maxLevel: Int
                            get() = section.getInt("max")

                        override fun getExp(level: Int): Int {
                            return Coerce.toInteger(script.eval(SimpleBindings(mapOf("level" to level))))
                        }
                    }
                }
                else -> {
                    warning("${section.getString("experience.type")} experience not supported.")
                    return@forEach
                }
            }
            level[node] = LevelOption(algorithm, section.getInt("min"), section)
        }
    }
}