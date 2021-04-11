package ink.ptms.chemdah.core

import io.izzel.taboolib.internal.gson.JsonObject
import io.izzel.taboolib.internal.gson.JsonParser
import io.izzel.taboolib.internal.gson.JsonPrimitive
import io.izzel.taboolib.kotlin.Reflex.Companion.reflex
import io.izzel.taboolib.module.nms.nbt.NBTBase
import io.izzel.taboolib.module.nms.nbt.NBTCompound
import java.util.concurrent.ConcurrentHashMap

/**
 * Chemdah
 * ink.ptms.chemdah.core.DataContainer
 *
 * @author sky
 * @since 2021/3/2 12:00 上午
 */
class DataContainer {

    private val map = ConcurrentHashMap<String, Data>()
    private var locked = false

    var changed = false
        set(value) {
            if (!locked) {
                field = value
            }
        }

    fun unchanged(func: DataContainer.() -> Unit) {
        locked = true
        func(this)
        locked = false
    }

    constructor()
    constructor(map: Map<String, Data>) {
        this.map.putAll(map)
    }

    operator fun get(key: String) = map[key]

    operator fun get(key: String, def: Any) = map[key] ?: def.data()

    operator fun set(key: String, value: Any) {
        map[key] = value.data()
        changed = true
    }

    fun put(key: String, value: Any) {
        map[key] = value.data()
        changed = true
    }

    fun remove(key: String) {
        map.remove(key)
        changed = true
    }

    fun merge(meta: DataContainer) {
        map.putAll(meta.map)
        changed = true
    }

    fun clear() {
        map.clear()
        changed = true
    }

    fun containsKey(key: String) = map.containsKey(key)

    fun containsValue(value: Any) = map.containsValue(value.data())

    fun entries() = map.entries

    fun keys() = map.keys().toList()

    fun copy() = DataContainer(map)

    fun flush(): DataContainer {
        changed = false
        return this
    }

    fun removeIf(predicate: (Pair<String, Data>) -> Boolean) {
        map.entries.forEach {
            if (predicate(it.toPair())) {
                remove(it.key)
                changed = true
            }
        }
    }

    fun toNBT(): NBTCompound {
        return NBTCompound().also {
            map.forEach { (k, v) -> it[k] = NBTBase.toNBT(v.value) }
        }
    }

    fun toJson() = JsonObject().also {
        map.forEach { (k, v) ->
            it.add(k, JsonPrimitive(0).also { json ->
                json.reflex("value", v.value)
            })
        }
    }.toString()

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

        fun JsonObject.dataContainer(): DataContainer {
            return DataContainer(entrySet().map { it.key to it.value.asJsonPrimitive.reflex<Any>("value")!!.data() }.toMap())
        }

        fun NBTCompound.dataContainer(): DataContainer {
            return DataContainer(map { it.key to it.value.reflex<Any>("data")!!.data() }.toMap())
        }

        fun fromJson(source: String): DataContainer {
            return JsonParser.parseString(source).asJsonObject.dataContainer()
        }
    }
}