package ink.ptms.chemdah.core

import ink.ptms.chemdah.core.CMaterial.Companion.toMaterials
import ink.ptms.chemdah.core.CPosition.Companion.toCosition
import ink.ptms.chemdah.util.asList
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.util.Coerce

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