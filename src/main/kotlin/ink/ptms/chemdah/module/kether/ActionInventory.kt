package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.core.quest.selector.InferItem
import ink.ptms.chemdah.core.quest.selector.InferItem.Companion.toInferItem
import ink.ptms.chemdah.util.getPlayer
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import taboolib.platform.util.isNotAir
import taboolib.type.BukkitEquipment
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionInventory
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionInventory {

    class InventoryTake(val item: InferItem.Item, val amount: Int) : ScriptAction<Boolean>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
            return CompletableFuture.completedFuture(item.take(frame.getPlayer().inventory, amount))
        }
    }

    class InventoryCheck(val item: InferItem.Item, val amount: Int) : ScriptAction<Boolean>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
            return CompletableFuture.completedFuture(item.check(frame.getPlayer().inventory, amount))
        }
    }

    class InventorySlot(val slot: Int, val item: InferItem.Item, val amount: Int) : ScriptAction<Boolean>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
            val equipment = frame.getPlayer().inventory.getItem(slot)
            return if (equipment.isNotAir() && item.match(equipment!!)) {
                CompletableFuture.completedFuture(equipment.amount >= amount)
            } else {
                CompletableFuture.completedFuture(false)
            }
        }
    }

    class InventoryEquipment(val equipment: BukkitEquipment, val item: InferItem.Item, val amount: Int) : ScriptAction<Boolean>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
            val equipment = equipment.getItem(frame.getPlayer())
            return if (equipment.isNotAir() && item.match(equipment!!)) {
                CompletableFuture.completedFuture(equipment.amount >= amount)
            } else {
                CompletableFuture.completedFuture(false)
            }
        }
    }

    class InventoryClose() : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            frame.getPlayer().closeInventory()
            return CompletableFuture.completedFuture(null)
        }
    }

    companion object {

        /**
         * inventory close
         * inventory check "minecraft:stone" amount 1
         * inventory take "minecraft:stone" amount 1
         * inventory helmet is "minecraft:stone"
         * inventory slot 9 is "minecraft:stone"
         */
        @KetherParser(["inventory"], shared = true)
        fun parser() = scriptParser {
            when (it.expects(
                "has", "have", "check", "take", "remove",
                "hand", "mainhand", "offhand",
                "head", "helmet", "chest", "chestplate", "legs", "leggings", "boots", "feet",
                "slot",
                "close"
            )) {
                "has", "have", "check" -> InventoryCheck(it.nextToken().toInferItem(), matchAmount(it))
                "take", "remove" -> InventoryTake(it.nextToken().toInferItem(), matchAmount(it))
                "hand", "mainhand" -> InventoryEquipment(BukkitEquipment.HAND, matchItem(it), matchAmount(it))
                "offhand" -> InventoryEquipment(BukkitEquipment.OFF_HAND, matchItem(it), matchAmount(it))
                "head", "helmet" -> InventoryEquipment(BukkitEquipment.HEAD, matchItem(it), matchAmount(it))
                "chest", "chestplate" -> InventoryEquipment(BukkitEquipment.CHEST, matchItem(it), matchAmount(it))
                "legs", "leggings" -> InventoryEquipment(BukkitEquipment.LEGS, matchItem(it), matchAmount(it))
                "boots", "feet" -> InventoryEquipment(BukkitEquipment.FEET, matchItem(it), matchAmount(it))
                "slot" -> InventorySlot(it.nextInt(), matchItem(it), matchAmount(it))
                "close" -> InventoryClose()
                else -> error("out of case")
            }
        }

        private fun matchItem(it: QuestReader) = it.run {
            it.expect("is")
            it.nextToken().toInferItem()
        }

        private fun matchAmount(it: QuestReader) = try {
            it.mark()
            it.expect("amount")
            it.nextInt()
        } catch (ex: Throwable) {
            it.reset()
            1
        }
    }
}