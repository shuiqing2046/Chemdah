package ink.ptms.chemdah.core.quest.selector

import ink.ptms.chemdah.api.event.InferItemHookEvent
import ink.ptms.chemdah.core.quest.selector.Flags.Companion.matchType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.PotionMeta
import taboolib.common.platform.function.warning
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.common5.Coerce
import taboolib.module.nms.getItemTag
import taboolib.module.nms.getName
import taboolib.platform.util.hasItem
import taboolib.platform.util.takeItem

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
        return inventory.hasItem(amount) { items.any { item -> item.match(it) } }
    }

    fun take(inventory: Inventory, amount: Int): Boolean {
        return inventory.takeItem(amount) { items.any { item -> item.match(it) } }
    }

    open class Item(val material: String, val flags: List<Flags>, val data: Map<String, String>) {

        open fun match(item: ItemStack) = matchType(item.type.name.toLowerCase()) && matchMetaData(item)

        open fun matchType(type: String) = flags.any { it.match(type, material) }

        open fun matchMetaData(item: ItemStack): Boolean {
            val meta = item.itemMeta
            return data.all {
                when (it.key) {
                    "name" -> it.value in item.getName()
                    "lore" -> meta?.lore?.toString()?.contains(it.value) == true
                    "custom-model-data" -> meta?.customModelData == Coerce.toInteger(it.value)
                    "enchant", "enchants", "enchantment" -> meta?.enchants?.any { e -> e.key.name.equals(it.value, true) } == true
                    "potion", "potions" -> if (meta is PotionMeta) {
                        meta.basePotionData.type.name.equals(it.value, true) || meta.customEffects.any { e -> e.type.name.equals(it.value, true) }
                    } else {
                        false
                    }
                    else -> if (it.key.startsWith("nbt.")) {
                        item.getItemTag().getDeep(it.key.substring("nbt.".length))?.asString().equals(it.value, true)
                    } else {
                        matchMetaData(item, meta, it.key, it.value)
                    }
                }
            }
        }

        open fun matchMetaData(item: ItemStack, itemMeta: ItemMeta?, key: String, value: String): Boolean {
            warning("$material[$key=$value] not supported.")
            return false
        }

        open fun check(inventory: Inventory, amount: Int): Boolean {
            return inventory.hasItem(amount) { match(it) }
        }

        open fun take(inventory: Inventory, amount: Int): Boolean {
            return inventory.takeItem(amount) { match(it) }
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
            return item.invokeConstructor(type.matchType(flag), flag, data) as Item
        }
    }
}