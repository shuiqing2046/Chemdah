package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.core.Metadata
import ink.ptms.chemdah.core.Metadata.Companion.data

class QuestMetaOperator(val profile: PlayerProfile, val task: Task) {

    val metadata: Metadata?
        get() = profile.quest[task.template.id]?.metadata

    fun get(node: String) = metadata?.get("${task.metaNode}.$node")

    fun get(node: String, def: Any) = metadata?.get("${task.metaNode}.$node") ?: def.data()

    fun put(node: String, value: Any) = metadata?.put("${task.metaNode}.$node", value)

    fun remove(node: String) = metadata?.remove("${task.metaNode}.$node")

    fun containsKey(node: String) = metadata?.containsKey("${task.metaNode}.$node")
}