package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.core.DataContainer.Companion.unsafeData
import ink.ptms.chemdah.core.PlayerProfile

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.QuestMetaOperator
 *
 * @author sky
 * @since 2021/3/2 1:13 上午
 */
class QuestDataOperator(val profile: PlayerProfile, val task: Task) {

    val container = profile.getQuests(openAPI = true).firstOrNull { it.id == task.template.id }?.persistentDataContainer

    fun containsKey(node: String) = container?.containsKey("${task.id}.$node")

    fun get(node: String) = container?.get("${task.id}.$node")

    fun get(node: String, def: Any) = container?.get("${task.id}.$node") ?: def.unsafeData()

    fun put(node: String, value: Any) = container?.set("${task.id}.$node", value)

    fun remove(node: String) = container?.remove("${task.id}.$node")

    fun clear() {
        container?.run { removeIf { it.first.startsWith(task.id) } }
    }
}