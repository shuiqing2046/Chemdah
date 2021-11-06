package ink.ptms.chemdah.core

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import io.netty.util.internal.ConcurrentSet
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.setProperty
import taboolib.module.nms.ItemTag
import taboolib.module.nms.ItemTagData
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Function

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

    fun unchanged(func: Consumer<DataContainer>) {
        locked = true
        func.accept(this)
        locked = false
    }

    internal fun unchanged(func: DataContainer.() -> Unit) {
        unchanged(Consumer { func(this) })
    }

    constructor()
    constructor(map: Map<String, Data>) {
        this.map.putAll(map)
    }

    /**
     * 获取数据
     */
    operator fun get(key: String): Data? {
        return map[key]
    }

    /**
     * 获取数据并返回默认值
     */
    operator fun get(key: String, def: Any): Data {
        return map[key] ?: def.unsafeData()
    }

    /**
     * 修改数据
     */
    operator fun set(key: String, value: Any) {
        map[key] = if (value is Data) value.change() else value.unsafeData().change()
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
        meta.forEach { (key, data) -> this[key] = data.data }
    }

    fun containsKey(key: String): Boolean {
        return map.containsKey(key)
    }

    fun containsValue(value: Any): Boolean {
        return map.containsValue(value.unsafeData())
    }

    fun entries(): MutableSet<MutableMap.MutableEntry<String, Data>> {
        return map.entries
    }

    fun keys(): List<String> {
        return map.keys().toList()
    }

    fun copy(): DataContainer {
        return DataContainer(map)
    }

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

    fun removeIf(predicate: Function<Map.Entry<String, Data>, Boolean>) {
        map.entries.forEach {
            if (predicate.apply(it)) {
                remove(it.key)
            }
        }
    }

    fun forEach(consumer: Consumer<Map.Entry<String, Data>>) {
        map.forEach { consumer.accept(it) }
    }

    fun toMap(): Map<String, Any> {
        return map.mapValues { it.value.data }
    }

    fun toNBT(): ItemTag {
        return ItemTag().also {
            map.forEach { (k, v) -> it[k] = ItemTagData.toNBT(v.data) }
        }
    }

    fun toJson() = JsonObject().also {
        map.forEach { (k, v) ->
            it.add(k, JsonPrimitive(0).also { json ->
                json.setProperty("value", v.data)
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

        fun Any.unsafeData(): Data {
            return Data.unsafeData(this)
        }

        fun JsonObject.dataContainer(): DataContainer {
            return DataContainer(entrySet().associate { it.key to it.value.asJsonPrimitive.getProperty<Any>("value")!!.unsafeData() })
        }

        fun ItemTag.dataContainer(): DataContainer {
            return DataContainer(entries.associate { it.key to it.value.getProperty<Any>("data")!!.unsafeData() })
        }

        fun fromJson(source: String): DataContainer {
            return JsonParser().parse(source).asJsonObject.dataContainer()
        }
    }
}