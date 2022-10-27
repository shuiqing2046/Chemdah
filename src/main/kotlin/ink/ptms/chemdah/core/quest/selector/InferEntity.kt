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

    open class Entity(val name: String, val flags: List<Flags>, val data: List<DataMatch>) {

        open fun match(entity: org.bukkit.entity.Entity) = matchType(entity.type.name.lowercase()) && matchData(entity)

        open fun matchType(type: String) = flags.any { it.match(type, name) }

        open fun matchData(entity: org.bukkit.entity.Entity): Boolean {
            return data.all {
                when (it.key) {
                    "name" -> it.check(entity.getI18nName())
                    else -> {
                        warning("$name[${it.key}=${it.value}] not supported.")
                        false
                    }
                }
            }
        }
    }

    @Suppress("IdentifierGrammar")
    class CitizensEntity(material: String, flags: List<Flags>, data: List<DataMatch>) : Entity(material, flags, data) {

        override fun match(entity: org.bukkit.entity.Entity): Boolean {
            return matchType(entity.citizensId()) && matchData(entity)
        }

        override fun matchData(entity: org.bukkit.entity.Entity): Boolean {
            val npc = CitizensAPI.getNPCRegistry().getNPC(entity)
            return data.all {
                when (it.key) {
                    "type" -> it.check(npc.entity.type.name)
                    "name" -> it.check(npc.fullName)
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

    class MythicMobsEntity(material: String, flags: List<Flags>, data: List<DataMatch>) : Entity(material, flags, data) {

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
                    "type" -> it.check(mob.entityType.name)
                    "name" -> it.check(entity.getI18nName())
                    "level" -> Coerce.toDouble(it.value) <= mob.level
                    "stance" -> it.value == mob.stance
                    "faction" -> it.value == mob.faction
                    // 配置文件中的属性
                    else -> it.check(mob.config.getString(it.key) ?: return false)
                }
            }
        }
    }

    companion object {

        fun List<String>.toInferEntity() = InferEntity(map { it.toInferEntity() })

        @Suppress("DuplicatedCode")
        fun String.toInferEntity(): Entity {
            var type: String
            val data = arrayListOf<DataMatch>()
            val flag = ArrayList<Flags>()
            if (indexOf('[') > -1 && endsWith(']')) {
                type = substring(0, indexOf('['))
                data += substring(indexOf('[') + 1, length - 1).split(',').map { DataMatch.fromString(it.trim()) }
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
            return entity.invokeConstructor(type.matchType(flag), flag, data)
        }
    }
}