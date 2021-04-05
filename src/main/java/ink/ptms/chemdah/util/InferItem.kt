package ink.ptms.chemdah.util

import ink.ptms.zaphkiel.ZaphkielAPI
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.Reflection
import io.izzel.taboolib.util.item.Items
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.util.InferItem
 *
 * [minecraft:]stone —— Vanilla Item
 * [minecraft:]stone[name=123,lore=123,enchant=123]
 *
 * zaphkiel:123[weight=1] —— Zaphkiel Item
 *
 * @author sky
 * @since 2021/4/5 9:32 下午
 */
abstract class InferItem(val material: String, val flags: List<TypeFlags>, val data: Map<String, String>) {

    fun match(item: ItemStack) = matchFlags(item.type.name.toLowerCase()) && matchMetaData(item)

    open fun matchFlags(type: String) = flags.any { it.match(type, material) }

    open fun matchMetaData(item: ItemStack): Boolean {
        val meta = item.itemMeta
        return data.all {
            when (it.key) {
                "name" -> it.value in Items.getName(item)
                "lore" -> meta?.lore?.toString()?.contains(it.value) == true
                "enchant", "enchants", "enchantment" -> meta.enchants.any { e -> e.key.name.contains(it.value) }
                else -> {
                    warning("$material[${it.key}=${it.value}] not supported.")
                    false
                }
            }
        }
    }

    abstract fun check(inventory: Inventory, amount: Int): Boolean

    abstract fun take(inventory: Inventory, amount: Int): Boolean

    class MinecraftItem(material: String, flags: List<TypeFlags>, data: Map<String, String>) : InferItem(material, flags, data) {

        override fun check(inventory: Inventory, amount: Int): Boolean {
            return Items.hasItem(inventory, { item -> matchFlags(item.type.name.toLowerCase()) && matchMetaData(item) }, amount)
        }

        override fun take(inventory: Inventory, amount: Int): Boolean {
            return Items.takeItem(inventory, { item -> matchFlags(item.type.name.toLowerCase()) && matchMetaData(item) }, amount)
        }
    }

    class ZaphkielItem(material: String, flags: List<TypeFlags>, data: Map<String, String>) : InferItem(material, flags, data) {

        override fun check(inventory: Inventory, amount: Int): Boolean {
            return Items.hasItem(inventory, { item -> matchFlags(item.zaphkielId()) && matchMetaData(item) }, amount)
        }

        override fun take(inventory: Inventory, amount: Int): Boolean {
            return Items.takeItem(inventory, { item -> matchFlags(item.zaphkielId()) && matchMetaData(item) }, amount)
        }

        override fun matchMetaData(item: ItemStack): Boolean {
            val itemStream = ZaphkielAPI.read(item)
            val meta = item.itemMeta
            return data.all {
                when (it.key) {
                    "name" -> it.value in Items.getName(item)
                    "lore" -> meta?.lore?.toString()?.contains(it.value) == true
                    "enchant", "enchants", "enchantment" -> meta.enchants.any { e -> e.key.name.contains(it.value) }
                    else -> itemStream.getZaphkielData()[it.key]?.asString()?.contains(it.value) == true
                }
            }
        }

        fun ItemStack.zaphkielId(): String {
            val itemStream = ZaphkielAPI.read(this)
            return if (itemStream.isExtension()) itemStream.getZaphkielName() else "@vanilla"
        }
    }

    companion object {

        fun String.toInferItem(): InferItem {
            var type: String
            val data = HashMap<String, String>()
            val flag = ArrayList<TypeFlags>()
            if (indexOf('[') > -1 && endsWith(']')) {
                type = substring(0, indexOf('['))
                data.putAll(substring(indexOf('[') + 1, length - 1).split("[,;]".toRegex()).map {
                    it.trim().split("=").run { get(0) to (getOrNull(1) ?: get(0)) }
                })
            } else {
                type = this
            }
            val item = when {
                type.startsWith("zaphkiel:") -> {
                    type = type.substring("zaphkiel:".length)
                    ZaphkielItem::class.java
                }
                type.startsWith("minecraft:") -> {
                    type = type.substring("minecraft:".length)
                    MinecraftItem::class.java
                }
                else -> {
                    MinecraftItem::class.java
                }
            }
            if (type == "*") {
                flag.add(TypeFlags.ALL)
            } else if (type.startsWith('%') && type.startsWith('%')) {
                type = type.substring(1, type.length - 1)
                flag.add(TypeFlags.TAG)
            } else {
                if (type.startsWith('(') && type.startsWith(')')) {
                    type = type.substring(1, type.length - 1)
                    flag.add(TypeFlags.CONTAINS)
                }
                if (type.startsWith('^')) {
                    type = type.substring(1)
                    flag.add(TypeFlags.STARTS_WITH)
                }
                if (type.endsWith('$')) {
                    type = type.substring(0, type.length - 1)
                    flag.add(TypeFlags.ENDS_WITH)
                }
            }
            flag.add(TypeFlags.DEFAULT)
            return Reflection.instantiateObject(item, type, flag, data) as InferItem
        }
    }
}