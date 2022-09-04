package ink.ptms.chemdah.module.kether

import ink.ptms.chemdah.core.quest.selector.InferItem
import ink.ptms.chemdah.core.quest.selector.InferItem.Companion.toInferItem
import ink.ptms.chemdah.util.getBukkitPlayer
import taboolib.library.kether.ParsedAction
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

    class InventoryTake(val item: InferItem.Item, val amount: ParsedAction<*>) : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            return frame.run(amount).int { a -> item.take(frame.getBukkitPlayer().inventory, a) }
        }
    }

    class InventoryCount(val item: InferItem.Item) : ScriptAction<Int>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Int> {
            var checkAmount = 0
            frame.getBukkitPlayer().inventory.contents.forEach { itemStack ->
                if (itemStack.isNotAir() && item.match(itemStack)) {
                    checkAmount += itemStack.amount
                }
            }
            return CompletableFuture.completedFuture(checkAmount)
        }
    }

    class InventoryCheck(val item: InferItem.Item, val amount: ParsedAction<*>) : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            return frame.run(amount).int { a -> item.check(frame.getBukkitPlayer().inventory, a) }
        }
    }

    class InventorySlot(val slot: ParsedAction<*>, val item: InferItem.Item, val amount: ParsedAction<*>) : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            return frame.run(slot).int { slot ->
                frame.run(amount).int { amount ->
                    val equipment = frame.getBukkitPlayer().inventory.getItem(slot)
                    if (equipment.isNotAir() && item.match(equipment!!)) {
                        equipment.amount >= amount
                    } else {
                        false
                    }
                }.join()
            }
        }
    }

    class InventoryEquipment(val equipment: BukkitEquipment, val item: InferItem.Item, val amount: ParsedAction<*>) : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            return frame.run(amount).int { amount ->
                val equipment = equipment.getItem(frame.getBukkitPlayer())
                if (equipment.isNotAir() && item.match(equipment!!)) {
                    equipment.amount >= amount
                } else {
                    false
                }
            }
        }
    }

    companion object {

        /**
         * inventory count "minecraft:stone"
         * inventory close
         * inventory check "minecraft:stone" amount 1
         * inventory take "minecraft:stone" amount 1
         * inventory helmet is "minecraft:stone"
         * inventory slot 9 is "minecraft:stone"
         */
        @KetherParser(["inventory"], shared = true)
        fun parser() = scriptParser {
            when (val token = it.nextToken()) {
                "close" -> actionNow { getBukkitPlayer().closeInventory() }
                "count", "amount" -> InventoryCount(it.nextToken().toInferItem())
                "has", "have", "check" -> InventoryCheck(it.nextToken().toInferItem(), matchAmount(it))
                "take", "remove" -> InventoryTake(it.nextToken().toInferItem(), matchAmount(it))
                "slot" -> {
                    it.mark()
                    try {
                        it.expects("is", "match")
                        InventorySlot(it.nextParsedAction(), it.nextToken().toInferItem(), matchAmount(it))
                    } catch (ex: Exception) {
                        it.reset()
                        val slot = it.nextParsedAction()
                        actionTake { run(slot).int { s -> getBukkitPlayer().inventory.getItem(s) } }
                    }
                }
                else -> {
                    val equip = BukkitEquipment.fromString(token) ?: error("Unknown inventory equipment: $token")
                    it.mark()
                    try {
                        it.expects("is", "match")
                        InventoryEquipment(equip, it.nextToken().toInferItem(), matchAmount(it))
                    } catch (ex: Exception) {
                        it.reset()
                        actionNow { equip.getItem(getBukkitPlayer()) }
                    }
                }
            }
        }

        private fun matchAmount(it: QuestReader) = try {
            it.mark()
            it.expect("amount")
            it.nextParsedAction()
        } catch (ex: Throwable) {
            it.reset()
            literalAction(1)
        }
    }
}