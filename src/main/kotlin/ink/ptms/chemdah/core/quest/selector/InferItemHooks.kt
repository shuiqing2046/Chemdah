package ink.ptms.chemdah.core.quest.selector

import com.ssomar.executableitems.api.ExecutableItemsAPI
import github.july_summer.julyitems.api.JItemAPI
import ink.ptms.chemdah.api.event.InferItemHookEvent
import ink.ptms.zaphkiel.ZaphkielAPI
import net.mmogroup.mmolib.MMOLib
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import su.nightexpress.quantumrpg.stats.items.ItemStats
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.getItemTag
import think.rpgitems.item.ItemManager

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.selector.item.InferItemCompat
 *
 * @author sky
 * @since 2021/4/23 10:30 下午
 */
@Suppress("SpellCheckingInspection")
internal object InferItemHooks  {

    @SubscribeEvent
    private fun onItemHook(e: InferItemHookEvent) {
        when (e.id.lowercase()) {
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
            "pxrpg" -> {
                e.itemClass = ItemPxRPG::class.java
            }
            "julyitem", "julyitems" -> {
                e.itemClass = ItemJulyItems::class.java
            }
            "eitems", "executableitems" -> {
                e.itemClass = ItemExecutableItems::class.java
            }
        }
    }

    class ItemZaphkiel(material: String, flags: List<Flags>, data: List<DataMatch>) : InferItem.Item(material, flags, data) {

        fun ItemStack.zaphkielId(): String {
            val itemStream = ZaphkielAPI.read(this)
            return if (itemStream.isExtension()) itemStream.getZaphkielName() else "@vanilla"
        }

        override fun match(item: ItemStack): Boolean {
            return matchType(item.zaphkielId()) && matchMetaData(item)
        }

        override fun matchMetaData(item: ItemStack, itemMeta: ItemMeta?, dataMatch: DataMatch): Boolean {
            return when {
                dataMatch.key.startsWith("config.") -> {
                    val itemStream = ZaphkielAPI.read(item)
                    if (itemStream.isVanilla()) return false
                    val data = itemStream.getZaphkielItem().config[dataMatch.key.substringAfter("data.")]
                    return if (data != null) dataMatch.check(data) else false
                }
                dataMatch.key.startsWith("data.") -> {
                    val itemStream = ZaphkielAPI.read(item)
                    if (itemStream.isVanilla()) return false
                    val data = itemStream.getZaphkielData().getDeep(dataMatch.key.substringAfter("data."))?.unsafeData()
                    return if (data != null) dataMatch.check(data) else false
                }
                else -> {
                    super.matchMetaData(item, itemMeta, dataMatch)
                }
            }
        }
    }

    class ItemMMOItem(material: String, flags: List<Flags>, data: List<DataMatch>) : InferItem.Item(material, flags, data) {

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
            return matchType(item.mmoId()) && matchMetaData(item)
        }

        override fun matchMetaData(item: ItemStack, itemMeta: ItemMeta?, dataMatch: DataMatch): Boolean {
            return when (dataMatch.key) {
                "set" -> dataMatch.check(item.mmoSet())
                "type" -> dataMatch.check(item.mmoType())
                else -> super.matchMetaData(item, itemMeta, dataMatch)
            }
        }
    }

    class ItemRPGItem(material: String, flags: List<Flags>, data: List<DataMatch>) : InferItem.Item(material, flags, data) {

        fun ItemStack.rpgName(): String {
            return ItemManager.toRPGItem(this).orElse(null)?.name ?: "@vanilla"
        }

        fun ItemStack.rpgUid(): Int {
            return ItemManager.toRPGItem(this).orElse(null)?.uid ?: -1
        }

        override fun match(item: ItemStack): Boolean {
            return matchType(item.rpgName()) && matchMetaData(item)
        }

        override fun matchMetaData(item: ItemStack, itemMeta: ItemMeta?, dataMatch: DataMatch): Boolean {
            return when (dataMatch.key) {
                "uid" -> dataMatch.check(item.rpgUid())
                else -> super.matchMetaData(item, itemMeta, dataMatch)
            }
        }
    }

    class ItemQuantumRPG(material: String, flags: List<Flags>, data: List<DataMatch>) : InferItem.Item(material, flags, data) {

        fun ItemStack.quantumName(): String {
            return ItemStats.getId(this) ?: "@vanilla"
        }

        fun ItemStack.quantumLevel(): Int {
            return ItemStats.getLevel(this)
        }

        override fun match(item: ItemStack): Boolean {
            return matchType(item.quantumName()) && matchMetaData(item)
        }

        override fun matchMetaData(item: ItemStack, itemMeta: ItemMeta?, dataMatch: DataMatch): Boolean {
            return when (dataMatch.key) {
                "level" -> dataMatch.check(item.quantumLevel())
                else -> super.matchMetaData(item, itemMeta, dataMatch)
            }
        }
    }

    class ItemPxRPG(material: String, flags: List<Flags>, data: List<DataMatch>) : InferItem.Item(material, flags, data) {

        fun ItemStack.pxId(): String {
            return getItemTag().getDeep("pxrpg.id")?.asString() ?: "@vanilla"
        }

        fun ItemStack.pxName(): String {
            return getItemTag().getDeep("pxrpg.name")?.asString() ?: "@vanilla"
        }

        fun ItemStack.pxAuthor(): String {
            return getItemTag().getDeep("pxrpg.authorName")?.asString() ?: "@vanilla"
        }

        fun ItemStack.pxQuality(): String {
            return getItemTag().getDeep("pxrpg.itemQuality")?.asString() ?: "@vanilla"
        }

        fun ItemStack.pxType(): String {
            return getItemTag().getDeep("pxrpg.itemType")?.asString() ?: "@vanilla"
        }

        fun ItemStack.pxTemplate(): String {
            return getItemTag().getDeep("pxrpg.template")?.asString() ?: "@vanilla"
        }

        fun ItemStack.pxLevel(): Int {
            return getItemTag().getDeep("pxrpg.level")?.asInt() ?: -1
        }

        fun ItemStack.pxBind(): String {
            return getItemTag().getDeep("pxrpg.bind")?.asString() ?: "@vanilla"
        }

        override fun match(item: ItemStack): Boolean {
            return matchType(item.pxId()) && matchMetaData(item)
        }

        override fun matchMetaData(item: ItemStack, itemMeta: ItemMeta?, dataMatch: DataMatch): Boolean {
            return when (dataMatch.key) {
                "name" -> dataMatch.check(item.pxName())
                "author" -> dataMatch.check(item.pxAuthor())
                "quality" -> dataMatch.check(item.pxQuality())
                "type" -> dataMatch.check(item.pxType())
                "template" -> dataMatch.check(item.pxTemplate())
                "level" -> dataMatch.check(item.pxLevel())
                "bind" -> dataMatch.check(item.pxBind())
                else -> super.matchMetaData(item, itemMeta, dataMatch)
            }
        }
    }

    class ItemJulyItems(material: String, flags: List<Flags>, data: List<DataMatch>) : InferItem.Item(material, flags, data) {

        fun ItemStack.julyName(): String {
            return JItemAPI.getInstance().toJItem(this)?.itemId ?: "@vanilla"
        }

        fun ItemStack.quantumLevel(): Int {
            return ItemStats.getLevel(this)
        }

        override fun match(item: ItemStack): Boolean {
            return matchType(item.julyName()) && matchMetaData(item)
        }
    }

    class ItemExecutableItems(material: String, flags: List<Flags>, data: List<DataMatch>) : InferItem.Item(material, flags, data) {

        fun ItemStack.executableId(): String {
            return ExecutableItemsAPI.getExecutableItemConfig(this)?.identification ?: "@vanilla"
        }

        override fun match(item: ItemStack): Boolean {
            return matchType(item.executableId()) && matchMetaData(item)
        }
    }
}