package ink.ptms.chemdah.core

import ink.ptms.chemdah.core.quest.selector.InferArea.Companion.toInferArea
import ink.ptms.chemdah.core.quest.selector.InferBlock.Companion.toInferBlock
import ink.ptms.chemdah.core.quest.selector.InferEntity.Companion.toInferEntity
import ink.ptms.chemdah.core.quest.selector.InferItem.Companion.toInferItem
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

    private val lazyPosition by lazy {
        value.toString().toInferArea(noWorld = false)
    }

    private val lazyVector by lazy {
        value.toString().toInferArea(noWorld = true)
    }

    private val lazyEntity by lazy {
        value.asList().toInferEntity()
    }

    private val lazyBlock by lazy {
        value.asList().toInferBlock()
    }

    private val lazyItem by lazy {
        value.asList().toInferItem()
    }

    fun toInt() = Coerce.toInteger(value)

    fun toFloat() = Coerce.toFloat(value)

    fun toDouble() = Coerce.toDouble(value)

    fun toLong() = Coerce.toLong(value)

    fun toShort() = Coerce.toShort(value)

    fun toByte() = Coerce.toByte(value)

    fun toBoolean() = Coerce.toBoolean(value)

    fun toVector() = lazyVector

    fun toPosition() = lazyPosition

    fun toInferEntity() = lazyEntity

    fun toInferBlock() = lazyBlock

    fun toInferItem() = lazyItem

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