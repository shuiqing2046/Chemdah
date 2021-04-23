package ink.ptms.chemdah.core.quest.selector

import ink.ptms.chemdah.api.event.single.InferItemHookEvent
import ink.ptms.zaphkiel.ZaphkielAPI
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.util.Coerce
import net.mmogroup.mmolib.MMOLib
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import su.nightexpress.quantumrpg.stats.items.ItemStats
import think.rpgitems.item.ItemManager

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.selector.item.InferItemCompat
 *
 * @author sky
 * @since 2021/4/23 10:30 下午
 */
@TListener
class InferItemHooks : Listener {

    @EventHandler
    fun e(e: InferItemHookEvent) {
        when (e.id) {
            "zaphkiel" -> {
                e.itemClass = ItemZaphkiel::class.java
            }
            "rpgitem", "rpgitems" -> {
                e.itemClass = ItemRPGItem::class.java
            }
            "mmoitem", "mmoitems" -> {
                e.itemClass = ItemMMOItem::class.java
            }
            "qrpg", "quantumrpg" -> {
                e.itemClass = ItemQuantumRPG::class.java
            }
        }
    }

    class ItemZaphkiel(material: String, flags: List<Flags>, data: Map<String, String>) : InferItem.Item(material, flags, data) {

        fun ItemStack.zaphkielId(): String {
            val itemStream = ZaphkielAPI.read(this)
            return if (itemStream.isExtension()) itemStream.getZaphkielName() else "@vanilla"
        }

        override fun match(item: ItemStack): Boolean {
            return matchFlags(item.zaphkielId()) && matchMetaData(item)
        }

        override fun matchMetaData(item: ItemStack, itemMeta: ItemMeta, key: String, value: String): Boolean {
            return if (key.startsWith("data.")) {
                ZaphkielAPI.read(item).getZaphkielData()[key.substring("data.".length)]?.asString().equals(value, true)
            } else {
                super.matchMetaData(item, itemMeta, key, value)
            }
        }
    }

    class ItemMMOItem(material: String, flags: List<Flags>, data: Map<String, String>) : InferItem.Item(material, flags, data) {

        fun ItemStack.mmoId(): String {
            val item = MMOLib.plugin.version.wrapper.getNBTItem(this)
            return item.getString("MMOITEMS_ITEM_ID") ?: "@vanilla"
        }

        fun ItemStack.mmoSet(): String {
            val item = MMOLib.plugin.version.wrapper.getNBTItem(this)
            return item.getString("MMOITEMS_ITEM_SET") ?: "@vanilla"
        }

        fun ItemStack.mmoType(): String {
            val item = MMOLib.plugin.version.wrapper.getNBTItem(this)
            return item.getString("MMOITEMS_ITEM_TYPE") ?: "@vanilla"
        }

        override fun match(item: ItemStack): Boolean {
            return matchFlags(item.mmoId()) && matchMetaData(item)
        }

        override fun matchMetaData(item: ItemStack, itemMeta: ItemMeta, key: String, value: String): Boolean {
            return when (key) {
                "set" -> item.mmoSet().equals(value, true)
                "type" -> item.mmoType().equals(value, true)
                else -> super.matchMetaData(item, itemMeta, key, value)
            }
        }
    }

    class ItemRPGItem(material: String, flags: List<Flags>, data: Map<String, String>) : InferItem.Item(material, flags, data) {

        fun ItemStack.rpgName(): String {
            return ItemManager.toRPGItem(this).orElse(null)?.name ?: "@vanilla"
        }

        fun ItemStack.rpgUid(): Int {
            return ItemManager.toRPGItem(this).orElse(null)?.uid ?: -1
        }

        override fun match(item: ItemStack): Boolean {
            return matchFlags(item.rpgName()) && matchMetaData(item)
        }

        override fun matchMetaData(item: ItemStack, itemMeta: ItemMeta, key: String, value: String): Boolean {
            return when (key) {
                "uid" -> item.rpgUid() == Coerce.toInteger(value)
                else -> super.matchMetaData(item, itemMeta, key, value)
            }
        }
    }

    class ItemQuantumRPG(material: String, flags: List<Flags>, data: Map<String, String>) : InferItem.Item(material, flags, data) {

        fun ItemStack.quantumName(): String {
            return ItemStats.getId(this) ?: "@vanilla"
        }

        fun ItemStack.quantumLevel(): Int {
            return ItemStats.getLevel(this)
        }

        override fun match(item: ItemStack): Boolean {
            return matchFlags(item.quantumName()) && matchMetaData(item)
        }

        override fun matchMetaData(item: ItemStack, itemMeta: ItemMeta, key: String, value: String): Boolean {
            return when (key) {
                "level" -> item.quantumLevel() <= Coerce.toInteger(value)
                else -> super.matchMetaData(item, itemMeta, key, value)
            }
        }
    }
}