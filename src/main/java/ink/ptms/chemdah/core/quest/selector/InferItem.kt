package ink.ptms.chemdah.core.quest.selector

import ink.ptms.chemdah.api.event.single.InferItemHookEvent
import ink.ptms.chemdah.core.quest.selector.Flags.Companion.matchFlags
import ink.ptms.chemdah.util.warning
import io.izzel.taboolib.module.nms.NMS
import io.izzel.taboolib.util.Reflection
import io.izzel.taboolib.util.item.Items
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
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

    open class Item(val material: String, val flags: List<Flags>, val data: Map<String, String>) {

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
                        matchMetaData(item, meta, it.key, it.value)
                    }
                }
            }
        }

        open fun matchMetaData(item: ItemStack, itemMeta: ItemMeta, key: String, value: String): Boolean {
            warning("$material[$key=$value] not supported.")
            return false
        }

        open fun check(inventory: Inventory, amount: Int): Boolean {
            return Items.hasItem(inventory, { match(it) }, amount)
        }

        open fun take(inventory: Inventory, amount: Int): Boolean {
            return Items.takeItem(inventory, { match(it) }, amount)
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
            val indexOfType = type.indexOf(':')
            val item = if (indexOfType in 0..(type.length - 2)) {
                val item = when (val namespace = type.substring(0, indexOfType)) {
                    "minecraft" -> Item::class.java
                    else -> InferItemHookEvent(namespace, Item::class.java).itemClass
                }
                type = type.substring(indexOfType + 1)
                item
            } else {
                Item::class.java
            }
            return Reflection.instantiateObject(item, type.matchFlags(flag), flag, data) as Item
        }
    }
}