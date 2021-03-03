package ink.ptms.chemdah.core

import java.util.concurrent.ConcurrentHashMap

/**
 * TabooCodeOriginal
 * ink.ptms.taboocode.api.CodeMeta
 *
 * @author sky
 * @since 2021/1/12 10:48 上午
 */
class DataContainer {

    private val map = ConcurrentHashMap<String, Data>()

    constructor()
    constructor(map: Map<String, Data>) {
        this.map.putAll(map)
    }

    operator fun get(key: String) = map[key]

    fun get(key: String, def: Any) = map[key] ?: def.data()

    fun put(key: String, value: Any) = map.put(key, value.data())

    fun remove(key: String) = map.remove(key)

    fun containsKey(key: String) = map.containsKey(key)

    fun containsValue(value: Any) = map.containsValue(value.data())

    fun merge(meta: DataContainer) = map.putAll(meta.map)

    fun entries() = map.entries

    fun clear() = map.clear()

    fun copy() = DataContainer(map)

    fun removeIf(predicate: (Pair<String, Data>) -> Boolean) {
        map.entries.forEach {
            if (predicate(it.toPair())) {
                remove(it.key)
            }
        }
    }

    override fun toString(): String {
        return "DataCenter(map=$map)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataContainer) return false
        if (map != other.map) return false
        return true
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    companion object {

        fun Any.data() = Data(this)
    }
}