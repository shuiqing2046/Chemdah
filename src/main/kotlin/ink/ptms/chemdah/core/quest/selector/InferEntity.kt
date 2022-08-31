package ink.ptms.chemdah.core.quest.selector

import ink.ptms.chemdah.api.event.InferEntityHookEvent
import ink.ptms.chemdah.core.quest.selector.Flags.Companion.matchType
import ink.ptms.um.Mythic
import net.citizensnpcs.api.CitizensAPI
import taboolib.common.platform.function.warning
import taboolib.common5.Coerce
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.module.nms.getI18nName

/**
 * Chemdah
 * ink.ptms.chemdah.util.selector.InferEntity
 *
 * @author sky
 * @since 2021/4/5 9:32 下午
 */
@Suppress("DuplicatedCode", "SpellCheckingInspection")
class InferEntity(val entities: List<Entity>) {

    fun isEntity(entity: org.bukkit.entity.Entity?) = entity != null && entities.any { it.match(entity) }

    open class Entity(val name: String, val flags: List<Flags>, val data: Map<String, String>) {

        open fun match(entity: org.bukkit.entity.Entity) = matchType(entity.type.name.lowercase()) && matchData(entity)

        open fun matchType(type: String) = flags.any { it.match(type, name) }

        open fun matchData(entity: org.bukkit.entity.Entity): Boolean {
            return data.all {
                when (it.key) {
                    "name" -> it.value in entity.getI18nName()
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
            return matchType(entity.citizensId()) && matchData(entity)
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

        fun org.bukkit.entity.Entity.mythicMobId(): String {
            return Mythic.API.getMob(this)?.id ?: "@vanilla"
        }

        override fun match(entity: org.bukkit.entity.Entity): Boolean {
            return matchType(entity.mythicMobId()) && matchData(entity)
        }

        override fun matchData(entity: org.bukkit.entity.Entity): Boolean {
            val mob = Mythic.API.getMob(entity) ?: return false
            return data.all {
                when (it.key) {
                    "type" -> it.value.equals(mob.entityType.name, true)
                    "name" -> it.value in entity.getI18nName()
                    "level" -> Coerce.toDouble(it.value) <= mob.level
                    "stance" -> it.value == mob.stance
                    "faction" -> it.value == mob.faction
                    else -> mob.config.getString(it.key)?.contains(it.value) == true
                }
            }
        }
    }

    companion object {

        fun List<String>.toInferEntity() = InferEntity(map { it.toInferEntity() })

        @Suppress("DuplicatedCode")
        fun String.toInferEntity(): Entity {
            var type: String
            val data = HashMap<String, String>()
            val flag = ArrayList<Flags>()
            if (indexOf('[') > -1 && endsWith(']')) {
                type = substring(0, indexOf('['))
                data.putAll(substring(indexOf('[') + 1, length - 1).split(",").map {
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
                    else -> InferEntityHookEvent(namespace, Entity::class.java).apply { call() }.itemClass
                }
                type = type.substring(indexOfType + 1)
                entity
            } else {
                Entity::class.java
            }
            return entity.invokeConstructor(type.matchType(flag), flag, data) as Entity
        }
    }
}