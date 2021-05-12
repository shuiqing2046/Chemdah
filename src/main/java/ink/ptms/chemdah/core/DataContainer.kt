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

    /**
     * 删除节点
     */
    val drops = ConcurrentSet<String>()

    /**
     * 数据是否发生变动
     */
    val isChanged: Boolean
        get() = drops.isNotEmpty() || map.any { it.value.changed }

    /**
     * 函数内的所有数据修改行为不会记录变动（不更新数据库）
     */
    fun unchanged(func: DataContainer.() -> Unit) {
        locked = true
        func(this)
        locked = false
    }

    constructor()
    constructor(map: Map<String, Data>) {
        this.map.putAll(map)
    }

    /**
     * 获取数据
     */
    operator fun get(key: String) = map[key]

    /**
     * 获取数据并返回默认值
     */
    operator fun get(key: String, def: Any) = map[key] ?: def.data()

    /**
     * 修改数据
     */
    operator fun set(key: String, value: Any) {
        map[key] = if (value is Data) value.change() else value.data().change()
        if (!locked) {
            drops.remove(key)
        }
    }

    /**
     * 删除数据
     */
    fun remove(key: String) {
        map.remove(key)
        if (!locked) {
            drops.add(key)
        }
    }

    /**
     * 清空数据
     */
    fun clear() {
        if (!locked) {
            drops.addAll(map.keys)
        }
        map.clear()
    }

    /**
     * 合并数据
     */
    fun merge(meta: DataContainer) {
        meta.forEach { key, data -> this[key] = data.data }
    }

    fun containsKey(key: String) = map.containsKey(key)

    fun containsValue(value: Any) = map.containsValue(value.data())

    fun entries() = map.entries

    fun keys() = map.keys().toList()

    fun copy() = DataContainer(map)

    /**
     * 释放变动
     */
    fun flush(): DataContainer {
        map.forEach {
            it.value.changed = false
        }
        drops.clear()
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
        return map.mapValues { it.value.data }
    }

    fun toNBT(): NBTCompound {
        return NBTCompound().also {
            map.forEach { (k, v) -> it[k] = NBTBase.toNBT(v.data) }
        }
    }

    fun toJson() = JsonObject().also {
        map.forEach { (k, v) ->
            it.add(k, JsonPrimitive(0).also { json ->
                json.reflex("value", v.data)
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

        fun Any.data() = Data.unsafeData(this)

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