package ink.ptms.chemdah.module.level

import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import taboolib.common.platform.Awake
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.warning
import taboolib.common5.Coerce
import taboolib.common5.compileJS
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.SecuredFile
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import java.util.concurrent.CompletableFuture
import javax.script.SimpleBindings

/**
 * Chemdah
 * ink.ptms.chemdah.module.level.LevelSystem
 *
 * @author sky
 * @since 2021/3/8 11:13 下午
 */
@Awake
object LevelSystem : Module {

    @Config("module/level.yml")
    lateinit var conf: Configuration
        private set

    val level = HashMap<String, LevelOption>()

    init {
        register()
    }

    fun getLevelOption(name: String): LevelOption? {
        return level[name]
    }

    fun PlayerProfile.setLevel(node: LevelOption, playerLevel: PlayerLevel) {
        setLevel(node, playerLevel.level, playerLevel.experience)
    }

    fun PlayerProfile.setLevel(node: LevelOption, level: Int, experience: Int) {
        val p = getLevel(node)
        val event = PlayerEvents.LevelChange(player, node, p.level, p.experience, level, experience)
        event.call()
        if (!event.isCancelled) {
            persistentDataContainer["module.level.${node.id}.level"] = event.newLevel
            persistentDataContainer["module.level.${node.id}.experience"] = event.newExperience
        }
    }

    fun PlayerProfile.getLevel(node: LevelOption): PlayerLevel {
        return PlayerLevel(
            persistentDataContainer["module.level.${node.id}.level", 0].toInt(),
            persistentDataContainer["module.level.${node.id}.experience", 0].toInt()
        )
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    internal fun onLevelChange(e: PlayerEvents.LevelChange) {
        if (e.newLevel > e.oldLevel) {
            ((e.oldLevel + 1)..e.newLevel).forEach { level ->
                e.option.getReward(level)?.eval(e.player, level)
            }
        }
    }

    override fun reload() {
        level.clear()
        conf.reload()
        conf.getKeys(false).forEach { node ->
            val section = conf.getConfigurationSection(node)!!
            val algorithm = when (section.getString("experience.type")) {
                "javascript" -> {
                    val script = section.getString("experience.math")!!.compileJS() ?: return@forEach
                    object : Level.Algorithm() {

                        override val maxLevel: Int
                            get() = section.getInt("max")

                        override fun getExp(level: Int): CompletableFuture<Int> {
                            return CompletableFuture.completedFuture(Coerce.toInteger(script.eval(SimpleBindings(mapOf("level" to level)))))
                        }
                    }
                }
                "kether" -> {
                    object : Level.Algorithm() {

                        override val maxLevel: Int
                            get() = section.getInt("max")

                        override fun getExp(level: Int): CompletableFuture<Int> {
                            return try {
                                KetherShell.eval(section.getString("experience.math").toString()) {
                                    rootFrame().variables().set("level", level)
                                }.thenApply {
                                    Coerce.toInteger(it)
                                }
                            } catch (ex: Exception) {
                                ex.printKetherErrorMessage()
                                CompletableFuture.completedFuture(0)
                            }
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