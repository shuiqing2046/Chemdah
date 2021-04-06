package ink.ptms.chemdah.util.selector

import ink.ptms.chemdah.util.selector.Flags.Companion.matchFlags
import ink.ptms.chemdah.util.warning
import io.izzel.taboolib.module.i18n.I18n
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.Reflection
import io.lumine.xikage.mythicmobs.MythicMobs

/**
 * Chemdah
 * ink.ptms.chemdah.util.selector.InferEntity
 *
 * @author sky
 * @since 2021/4/5 9:32 下午
 */
class InferEntity(val entities: List<Entity>) {

    fun isEntity(entity: org.bukkit.entity.Entity?) = entity != null && entities.any { it.match(entity) }

    abstract class Entity(val name: String, val flags: List<Flags>, val data: Map<String, String>) {

        open fun match(entity: org.bukkit.entity.Entity) = matchFlags(entity.type.name.toLowerCase()) && matchData(entity)

        open fun matchFlags(type: String) = flags.any { it.match(type, name) }

        open fun matchData(entity: org.bukkit.entity.Entity): Boolean {
            return data.all {
                when (it.key) {
                    "name" -> it.value in I18n.get().getName(entity)
                    else -> {
                        warning("$name[${it.key}=${it.value}] not supported.")
                        false
                    }
                }
            }
        }
    }

    class MinecraftEntity(material: String, flags: List<Flags>, data: Map<String, String>) : Entity(material, flags, data)

    class MythicMobsEntity(material: String, flags: List<Flags>, data: Map<String, String>) : Entity(material, flags, data) {

        override fun match(entity: org.bukkit.entity.Entity): Boolean {
            return matchFlags(entity.mythicMobId()) && matchData(entity)
        }

        override fun matchData(entity: org.bukkit.entity.Entity): Boolean {
            val mob = MythicMobs.inst().mobManager.getMythicMobInstance(entity)
            return data.all {
                when (it.key) {
                    "type" -> it.value.equals(mob.type.entityType, true)
                    "name" -> it.value in I18n.get().getName(entity)
                    "level" -> Coerce.toDouble(it.value) <= mob.level
                    "stance" -> it.value == mob.stance
                    "faction" -> it.value == mob.faction
                    else -> mob.type.config.getString(it.key)?.contains(it.value) == true
                }
            }
        }

        fun org.bukkit.entity.Entity.mythicMobId(): String {
            return MythicMobs.inst().mobManager.getMythicMobInstance(this)?.type?.internalName ?: "@vanilla"
        }
    }

    companion object {

        fun List<String>.toInferEntity() = InferEntity(map { it.toInferEntity() })

        fun String.toInferEntity(): Entity {
            var type: String
            val data = HashMap<String, String>()
            val flag = ArrayList<Flags>()
            if (indexOf('[') > -1 && endsWith(']')) {
                type = substring(0, indexOf('['))
                data.putAll(substring(indexOf('[') + 1, length - 1).split("[,;]".toRegex()).map {
                    it.trim().split("=").run { get(0) to (getOrNull(1) ?: get(0)) }
                })
            } else {
                type = this
            }
            val item = when {
                type.startsWith("mythicmobs:") -> {
                    type = type.substring("mythicmobs:".length)
                    MythicMobsEntity::class.java
                }
                type.startsWith("minecraft:") -> {
                    type = type.substring("minecraft:".length)
                    MinecraftEntity::class.java
                }
                else -> {
                    MinecraftEntity::class.java
                }
            }
            return Reflection.instantiateObject(item, type.matchFlags(flag), flag, data) as Entity
        }
    }
}