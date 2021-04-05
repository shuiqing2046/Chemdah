package ink.ptms.chemdah.core

import ink.ptms.chemdah.util.InferArea.Companion.toInferArea
import ink.ptms.chemdah.util.InferItem.Companion.toInferItem
import ink.ptms.chemdah.util.Mats.Companion.toMats
import ink.ptms.chemdah.util.asList
import io.izzel.taboolib.util.Coerce

/**
 * Chemdah
 * ink.ptms.chemdah.core.Data
 *
 * @author sky
 * @since 2021/3/2 12:00 上午
 */
class Data(val value: Any) {

    private val lazyMaterial by lazy {
        asList().map { it.trim().toLowerCase() }.toMats()
    }

    private val lazyPosition by lazy {
        toString().toInferArea()
    }

    private val lazyItem by lazy {
        toString().toInferItem()
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

    fun toItem() = lazyItem

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