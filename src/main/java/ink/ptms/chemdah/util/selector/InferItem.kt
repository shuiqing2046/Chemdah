package ink.ptms.chemdah.util.selector

import ink.ptms.chemdah.util.selector.Flags.Companion.matchFlags
import ink.ptms.chemdah.util.warning
import ink.ptms.zaphkiel.ZaphkielAPI
import io.izzel.taboolib.module.nms.NMS
import io.izzel.taboolib.util.Reflection
import io.izzel.taboolib.util.item.Items
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta

/**
 * Chemdah
 * ink.ptms.chemdah.util.selector.InferItem
 *
 * [minecraft:]stone —— Vanilla Item
 * [minecraft:]stone[name=123,lore=123,enchant=123]
 *
 * zaphkiel:123[weight=1] —— Zaphkiel Item
 *
 * @author sky
 * @since 2021/4/5 9:32 下午
 */
class InferItem(val items: List<Item>) {

    fun isItem(item: ItemStack) = items.any { it.match(item) }

    fun check(inventory: Inventory, amount: Int): Boolean {
        return Items.hasItem(inventory, { items.any { item -> item.match(it) } }, amount)
    }

    fun take(inventory: Inventory, amount: Int): Boolean {
        return Items.takeItem(inventory, { items.any { item -> item.match(it) } }, amount)
    }

    abstract class Item(val material: String, val flags: List<Flags>, val data: Map<String, String>) {

        open fun match(item: ItemStack) = matchFlags(item.type.name.toLowerCase()) && matchMetaData(item)

        open fun matchFlags(type: String) = flags.any { it.match(type, material) }

        open fun matchMetaData(item: ItemStack): Boolean {
            val meta = item.itemMeta
            return data.all {
                when (it.key) {
                    "name" -> it.value in Items.getName(item)
                    "lore" -> meta?.lore?.toString()?.contains(it.value) == true
                    "enchant", "enchants", "enchantment" -> meta.enchants.any { e -> e.key.name.equals(it.value, true) }
                    "potion", "potions" -> if (meta is PotionMeta) {
                        meta.basePotionData.type.name.equals(it.value, true) || meta.customEffects.any { e -> e.type.name.equals(it.value, true) }
                    } else {
                        false
                    }
                    else -> if (it.key.startsWith("nbt.")) {
                        NMS.handle().loadNBT(item).getDeep(it.key.substring("nbt.".length))?.asString().equals(it.value, true)
                    } else {
                        warning("$material[${it.key}=${it.value}] not supported.")
                        false
                    }
                }
            }
        }

        open fun check(inventory: Inventory, amount: Int): Boolean {
            return Items.hasItem(inventory, { match(it) }, amount)
        }

        open fun take(inventory: Inventory, amount: Int): Boolean {
            return Items.takeItem(inventory, { match(it) }, amount)
        }
    }

    class MinecraftItem(material: String, flags: List<Flags>, data: Map<String, String>) : Item(material, flags, data)

    class ZaphkielItem(material: String, flags: List<Flags>, data: Map<String, String>) : Item(material, flags, data) {

        override fun match(item: ItemStack): Boolean {
            return matchFlags(item.zaphkielId()) && matchMetaData(item)
        }

        override fun matchMetaData(item: ItemStack): Boolean {
            val meta = item.itemMeta
            return data.all {
                when (it.key) {
                    "name" -> it.value in Items.getName(item)
                    "lore" -> meta?.lore?.toString()?.contains(it.value) == true
                    "enchant", "enchants", "enchantment" -> meta.enchants.any { e -> e.key.name.contains(it.value) }
                    "potion", "potions" -> if (meta is PotionMeta) {
                        meta.basePotionData.type.name.equals(it.value, true) || meta.customEffects.any { e -> e.type.name.equals(it.value, true) }
                    } else {
                        false
                    }
                    else -> when {
                        it.key.startsWith("nbt.") -> {
                            NMS.handle().loadNBT(item).getDeep(it.key.substring("nbt.".length))?.asString().equals(it.value, true)
                        }
                        it.key.startsWith("data.") -> {
                            ZaphkielAPI.read(item).getZaphkielData()[it.key.substring("data.".length)]?.asString().equals(it.value, true)
                        }
                        else -> {
                            warning("$material[${it.key}=${it.value}] not supported.")
                            false
                        }
                    }
                }
            }
        }

        fun ItemStack.zaphkielId(): String {
            val itemStream = ZaphkielAPI.read(this)
            return if (itemStream.isExtension()) itemStream.getZaphkielName() else "@vanilla"
        }
    }

    companion object {

        fun List<String>.toInferItem() = InferItem(map { it.toInferItem() })

        fun String.toInferItem(): Item {
            var type: String
            val data = HashMap<String, String>()
            val flag = ArrayList<Flags>()
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
            return Reflection.instantiateObject(item, type.matchFlags(flag), flag, data) as Item
        }
    }
}