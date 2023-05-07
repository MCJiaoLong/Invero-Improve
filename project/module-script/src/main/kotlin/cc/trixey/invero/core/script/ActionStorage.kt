package cc.trixey.invero.core.script

import cc.trixey.invero.core.script.loader.InveroKetherParser
import cc.trixey.invero.ui.bukkit.panel.CraftingPanel
import org.bukkit.inventory.ItemStack
import taboolib.module.kether.combinationParser
import taboolib.platform.util.isAir
import taboolib.platform.util.isNotAir

/**
 * Invero
 * cc.trixey.invero.core.script.kether.ActionStorage
 *
 * @author Arasple
 * @since 2023/2/10 15:38
 */
object ActionStorage {


    /*
    storage at 6 exist | empty -> Boolean
    storage at 6 delete
    storage at 6 get -> ItemStack
    storage at 6 set to <value: ItemStack>
    storage at 6 isLocked
    storage at 6 free
    storage at 6 lock
     */
    @InveroKetherParser(["storage", "crafting"])
    fun parser() = combinationParser {
        it.group(
            command("at", "by", then = int()), symbol(), command("to", then = action()).option().defaultsTo(null)
        ).apply(it) { slot, method, value ->

            now {
                val panel = findNearstPanel() ?: selfPanel<CraftingPanel>() ?: error("Not found crafting panel")

                when (method) {
                    "get" -> panel.getStorageItem(slot)
                    "exist", "exists" -> panel.getStorageItem(slot).isNotAir()
                    "empty" -> panel.getStorageItem(slot).isAir
                    "delete", "del" -> {
                        panel.delete(slot)
                        panel.runCallback()
                    }

                    "set" -> {
                        if (value != null) {
                            val item = newFrame(value).run<ItemStack?>().getNow(null)
                            panel.set(slot, item)
                            panel.runCallback()
                        } else "INVALID ITEM"
                    }

                    "isFree" -> slot in panel.freeSlots
                    "isLocked" -> slot !in panel.freeSlots
                    "free" -> panel.free(slot)
                    "lock" -> panel.lock(slot)
                    else -> {
                        error("Unknown operator $method at $slot")
                    }
                }
            }
        }
    }

}