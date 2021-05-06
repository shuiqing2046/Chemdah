package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.core.DataContainer.Companion.data
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

    fun containsKey(node: String) = container?.containsKey("${task.metaNode}.$node")

    fun get(node: String) = container?.get("${task.metaNode}.$node")

    fun get(node: String, def: Any) = container?.get("${task.metaNode}.$node") ?: def.data()

    fun put(node: String, value: Any) = container?.set("${task.metaNode}.$node", value)

    fun remove(node: String) = container?.remove("${task.metaNode}.$node")

    fun clear() {
        container?.run { removeIf { it.first.startsWith(task.metaNode) } }
    }
}