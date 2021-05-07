package ink.ptms.chemdah.core.quest.selector

import ink.ptms.chemdah.api.event.InferEntityHookEvent
import ink.ptms.chemdah.core.quest.selector.Flags.Companion.matchType
import ink.ptms.chemdah.util.warning
import io.izzel.taboolib.module.i18n.I18n
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.Reflection
import io.lumine.xikage.mythicmobs.MythicMobs
import net.citizensnpcs.api.CitizensAPI

/**
 * Chemdah
 * ink.ptms.chemdah.util.selector.InferEntity
 *
 * @author sky
 * @since 2021/4/5 9:32 下午
 */
class InferEntity(val entities: List<Entity>) {

    fun isEntity(entity: org.bukkit.entity.Entity?) = entity != null && entities.any { it.match(entity) }

    open class Entity(val name: String, val flags: List<Flags>, val data: Map<String, String>) {

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

    class CitizensEntity(material: String, flags: List<Flags>, data: Map<String, String>) : Entity(material, flags, data) {

        override fun match(entity: org.bukkit.entity.Entity): Boolean {
            return matchFlags(entity.citizensId()) && matchData(entity)
        }

        override fun matchData(entity: org.bukkit.entity.Entity): Boolean {
            val npc = CitizensAPI.getNPCRegistry().getNPC(entity)
            return data.all {
                when (it.key) {
                    "type" -> it.value.equals(npc.entity.type.name, true)
                    "name" -> it.value in npc.fullName
                    else -> {
                        warning("$name[${it.key}=${it.value}] not supported.")
                        false
                    }
                }
            }
        }

        fun org.bukkit.entity.Entity.citizensId(): String {
            return CitizensAPI.getNPCRegistry().getNPC(this)?.id?.toString() ?: "@vanilla"
        }
    }

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
            val indexOfType = type.indexOf(':')
            val entity = if (indexOfType in 0..(type.length - 2)) {
                val entity = when (val namespace = type.substring(0, indexOfType)) {
                    "minecraft" -> Entity::class.java
                    "citizen", "citizens" -> CitizensEntity::class.java
                    "mythicmob", "mythicmobs" -> MythicMobsEntity::class.java
                    else -> InferEntityHookEvent(namespace, Entity::class.java).itemClass
                }
                type = type.substring(indexOfType + 1)
                entity
            } else {
                Entity::class.java
            }
            return Reflection.instantiateObject(entity, type.matchType(flag), flag, data) as Entity
        }
    }
}