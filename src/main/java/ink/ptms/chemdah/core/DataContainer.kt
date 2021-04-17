package ink.ptms.chemdah.core

import io.izzel.taboolib.internal.gson.JsonObject
import io.izzel.taboolib.internal.gson.JsonParser
import io.izzel.taboolib.internal.gson.JsonPrimitive
import io.izzel.taboolib.kotlin.Reflex.Companion.reflex
import io.izzel.taboolib.module.nms.nbt.NBTBase
import io.izzel.taboolib.module.nms.nbt.NBTCompound
import io.netty.util.internal.ConcurrentSet
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

    val released = ConcurrentSet<String>()

    val changed: Boolean
        get() = released.isNotEmpty() || map.any { it.value.changed }

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
        map[key] = value.data().change()
        if (!locked) {
            released.remove(key)
        }
    }

    fun put(key: String, value: Any) {
        map[key] = value.data().change()
        if (!locked) {
            released.remove(key)
        }
    }

    fun remove(key: String) {
        map.remove(key)
        if (!locked) {
            released.add(key)
        }
    }

    fun clear() {
        if (!locked) {
            released.addAll(map.keys)
        }
        map.clear()
    }

    fun merge(meta: DataContainer) {
        meta.forEach { key, data -> put(key, data.value) }
    }

    fun containsKey(key: String) = map.containsKey(key)

    fun containsValue(value: Any) = map.containsValue(value.data())

    fun entries() = map.entries

    fun keys() = map.keys().toList()

    fun copy() = DataContainer(map)

    fun flush(): DataContainer {
        map.forEach {
            it.value.changed = false
        }
        released.clear()
        return this
    }

    fun removeIf(predicate: (Pair<String, Data>) -> Boolean) {
        map.entries.forEach {
            if (predicate(it.toPair())) {
                remove(it.key)
            }
        }
    }

    fun forEach(consumer: (String, Data) -> (Unit)) {
        map.forEach { consumer(it.key, it.value) }
    }

    fun toMap(): Map<String, Any> {
        return map.mapValues { it.value.value }
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

    private fun Data.change(): Data {
        if (!locked) {
            changed = true
        }
        return this
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