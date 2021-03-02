package ink.ptms.chemdah.core

import ink.ptms.chemdah.core.Cosition.Companion.toCosition
import ink.ptms.chemdah.core.Materials.Companion.toMaterials
import ink.ptms.chemdah.util.asList
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.kotlin.Reflex
import io.izzel.taboolib.kotlin.Reflex.Companion.toReflex
import io.izzel.taboolib.module.nms.nbt.NBTBase
import io.izzel.taboolib.module.nms.nbt.NBTCompound
import io.izzel.taboolib.util.Coerce
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooCodeOriginal
 * ink.ptms.taboocode.api.CodeMeta
 *
 * @author sky
 * @since 2021/1/12 10:48 上午
 */
class Metadata {

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

    fun merge(meta: Metadata) = map.putAll(meta.map)

    fun entries() = map.entries

    fun clear() = map.clear()

    fun copy() = Metadata(map)

    fun compound() = NBTCompound().also {
        map.map { (k, v) ->
            it[k] = NBTBase.toNBT(v.value)
        }
    }

    override fun toString(): String {
        return "Metadata(map=$map)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Metadata) return false
        if (map != other.map) return false
        return true
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    companion object {

        fun Any.data() = Data(this)

        fun NBTCompound.toMetadata(): Metadata {
            return Metadata(map { it.key to Data(it.value.toReflex().read("data")!!) }.toMap())
        }
    }

    class Data(val value: Any) {

        private val lazyMaterial by lazy {
            asList().mapNotNull { XMaterial.matchXMaterial(it.trim()).orElse(null) }.toMaterials()
        }

        private val lazyPosition by lazy {
            toString().toCosition()
        }

        fun toInt() = Coerce.toInteger(value)

        fun toFloat() = Coerce.toFloat(value)

        fun toDouble() = Coerce.toDouble(value)

        fun toLong() = Coerce.toLong(value)

        fun toShort() = Coerce.toShort(value)

        fun toByte() = Coerce.toByte(value)

        fun toBoolean() = Coerce.toBoolean(value)

        fun toPosition() = lazyPosition

        fun toMaterial() = lazyMaterial

        fun asList() = value.asList()

        override fun toString() = value.toString()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Data) return false
            if (value != other.value) return false
            return true
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }
    }
}