package ink.ptms.chemdah.core

import ink.ptms.chemdah.core.quest.selector.InferArea
import ink.ptms.chemdah.core.quest.selector.InferArea.Companion.toInferArea
import ink.ptms.chemdah.core.quest.selector.InferBlock
import ink.ptms.chemdah.core.quest.selector.InferBlock.Companion.toInferBlock
import ink.ptms.chemdah.core.quest.selector.InferEntity
import ink.ptms.chemdah.core.quest.selector.InferEntity.Companion.toInferEntity
import ink.ptms.chemdah.core.quest.selector.InferItem
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
open class Data {

    val data: Any
    var changed = false

    protected var lazyCache: Any? = null

    constructor(value: Int) {
        this.data = value
    }

    constructor(value: Float) {
        this.data = value
    }

    constructor(value: Double) {
        this.data = value
    }

    constructor(value: Long) {
        this.data = value
    }

    constructor(value: Short) {
        this.data = value
    }

    constructor(value: Byte) {
        this.data = value
    }

    constructor(value: Boolean) {
        this.data = value
    }

    protected constructor(value: Any) {
        this.data = value
    }

    fun toInt() = Coerce.toInteger(data)

    fun toFloat() = Coerce.toFloat(data)

    fun toDouble() = Coerce.toDouble(data)

    fun toLong() = Coerce.toLong(data)

    fun toShort() = Coerce.toShort(data)

    fun toByte() = Coerce.toByte(data)

    fun toBoolean() = Coerce.toBoolean(data)

    fun toVector(): InferArea {
        if (lazyCache !is InferArea) {
            lazyCache = data.toString().toInferArea(true)
        }
        return lazyCache as InferArea
    }

    fun toPosition(): InferArea {
        if (lazyCache !is InferArea) {
            lazyCache = data.toString().toInferArea()
        }
        return lazyCache as InferArea
    }

    fun toInferEntity(): InferEntity {
        if (lazyCache !is InferEntity) {
            lazyCache = data.asList().toInferEntity()
        }
        return lazyCache as InferEntity
    }

    fun toInferBlock(): InferBlock {
        if (lazyCache !is InferBlock) {
            lazyCache = data.asList().toInferBlock()
        }
        return lazyCache as InferBlock
    }

    fun toInferItem(): InferItem {
        if (lazyCache !is InferItem) {
            lazyCache = data.asList().toInferItem()
        }
        return lazyCache as InferItem
    }

    fun asList() = data.asList()

    override fun toString() = data.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Data) return false
        if (data != other.data) return false
        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object {

        fun unsafeData(any: Any) = Data(any)
    }
}