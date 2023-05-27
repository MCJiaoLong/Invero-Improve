package cc.trixey.invero.core.script

import cc.trixey.invero.common.chemdah.InferItem
import cc.trixey.invero.common.chemdah.InferItem.Companion.toInferItem
import cc.trixey.invero.core.script.loader.InveroKetherParser
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import taboolib.platform.util.giveItem
import taboolib.platform.util.isNotAir
import taboolib.type.BukkitEquipment
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * cc.trixey.invero.core.script.ActionInventory
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

    class InventorySlot(val slot: ParsedAction<*>, val item: InferItem.Item, val amount: ParsedAction<*>) :
        ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            val future = CompletableFuture<Any?>()
            frame.run(slot).int { slot ->
                frame.run(amount).int { amount ->
                    val equipment = frame.getBukkitPlayer().inventory.getItem(slot)
                    val value = if (equipment.isNotAir() && item.match(equipment!!)) {
                        equipment.amount >= amount
                    } else {
                        false
                    }
                    future.complete(value)
                }
            }
            return future
        }
    }

    class InventoryEquipment(val equipment: BukkitEquipment, val item: InferItem.Item, val amount: ParsedAction<*>) :
        ScriptAction<Any?>() {

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
        @InveroKetherParser(["inventory"])
        fun parser() = scriptParser {
            when (val token = it.nextToken()) {
                "close" -> actionNow { getBukkitPlayer().closeInventory() }
                "count", "amount" -> InventoryCount(it.nextToken().lowercase().toInferItem())
                "has", "have", "check" -> InventoryCheck(it.nextToken().lowercase().toInferItem(), matchAmount(it))
                "take", "remove" -> InventoryTake(it.nextToken().lowercase().toInferItem(), matchAmount(it))
                "slot" -> {
                    it.mark()
                    try {
                        it.expects("is", "match")
                        InventorySlot(it.nextParsedAction(), it.nextToken().lowercase().toInferItem(), matchAmount(it))
                    } catch (ex: Exception) {
                        it.reset()
                        val slot = it.nextParsedAction()
                        actionTake { run(slot).int { s -> getBukkitPlayer().inventory.getItem(s) } }
                    }
                }

                "add", "give" -> {
                    val item = it.nextParsedAction()
                    actionFuture { future ->
                        newFrame(item).run<ItemStack>().thenApply { stack ->
                            future.complete(getBukkitPlayer().giveItem(stack))
                        }
                    }
                }

                else -> {
                    val equip = BukkitEquipment.fromString(token) ?: error("Unknown inventory equipment: $token")
                    it.mark()
                    try {
                        it.expects("is", "match")
                        InventoryEquipment(equip, it.nextToken().lowercase().toInferItem(), matchAmount(it))
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

        private fun ScriptFrame.getBukkitPlayer(): Player {
            return script().sender?.castSafely<Player>() ?: error("No player selected.")
        }

    }
}