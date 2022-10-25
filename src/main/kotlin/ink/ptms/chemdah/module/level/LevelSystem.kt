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

    /**
     * 设置玩家等级数据
     *
     * @param option 经验配置
     * @param playerLevel 玩家经验数据
     */
    fun PlayerProfile.setLevel(option: LevelOption, playerLevel: PlayerLevel) {
        setLevel(option, playerLevel.level, playerLevel.experience)
    }

    /**
     * 设置玩家等级数据
     *
     * @param option 经验配置
     * @param level 等级
     * @param experience 经验
     */
    fun PlayerProfile.setLevel(option: LevelOption, level: Int, experience: Int) {
        val p = getLevel(option)
        val event = PlayerEvents.LevelChange(player, option, p.level, p.experience, level, experience)
        if (event.call()) {
            persistentDataContainer["module.level.${option.id}.level"] = event.newLevel
            persistentDataContainer["module.level.${option.id}.experience"] = event.newExperience
        }
    }

    /**
     * 获取玩家等级数据
     *
     * @param option 经验配置
     * @return [PlayerLevel]
     */
    fun PlayerProfile.getLevel(option: LevelOption): PlayerLevel {
        return PlayerLevel(
            persistentDataContainer["module.level.${option.id}.level", 0].toInt(),
            persistentDataContainer["module.level.${option.id}.experience", 0].toInt()
        )
    }

    /**
     * 设置玩家等级
     *
     * @param option 经验配置
     * @param value 经验值
     * @return [CompletableFuture<PlayerLevel>]
     */
    fun PlayerProfile.setLevel(option: LevelOption, value: Int): CompletableFuture<PlayerLevel> {
        return CompletableFuture<PlayerLevel>().also { future ->
            val level = option.toLevel(getLevel(option))
            level.setLevel(value).thenAccept {
                val playerLevel = level.toPlayerLevel()
                setLevel(option, playerLevel)
                future.complete(playerLevel)
            }
        }
    }

    /**
     * 给予玩家等级
     *
     * @param option 经验配置
     * @param value 经验值
     * @return [CompletableFuture<PlayerLevel>]
     */
    fun PlayerProfile.giveLevel(option: LevelOption, value: Int): CompletableFuture<PlayerLevel> {
        return CompletableFuture<PlayerLevel>().also { future ->
            val level = option.toLevel(getLevel(option))
            level.addLevel(value).thenAccept {
                val playerLevel = level.toPlayerLevel()
                setLevel(option, playerLevel)
                future.complete(playerLevel)
            }
        }
    }

    /**
     * 设置玩家经验值
     *
     * @param option 经验配置
     * @param value 经验值
     * @return [CompletableFuture<PlayerLevel>]
     */
    fun PlayerProfile.setExperience(option: LevelOption, value: Int): CompletableFuture<PlayerLevel> {
        return CompletableFuture<PlayerLevel>().also { future ->
            val level = option.toLevel(getLevel(option))
            level.setExperience(value).thenAccept {
                val playerLevel = level.toPlayerLevel()
                setLevel(option, playerLevel)
                future.complete(playerLevel)
            }
        }
    }

    /**
     * 给予玩家经验值
     *
     * @param option 经验配置
     * @param value 经验值
     * @return [CompletableFuture<PlayerLevel>]
     */
    fun PlayerProfile.giveExperience(option: LevelOption, value: Int): CompletableFuture<PlayerLevel> {
        return CompletableFuture<PlayerLevel>().also { future ->
            val level = option.toLevel(getLevel(option))
            level.addExperience(value).thenAccept {
                val playerLevel = level.toPlayerLevel()
                setLevel(option, playerLevel)
                future.complete(playerLevel)
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onLevelChange(e: PlayerEvents.LevelChange) {
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