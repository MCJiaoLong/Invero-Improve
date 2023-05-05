package cc.trixey.invero.ui.bukkit

import cc.trixey.invero.ui.bukkit.nms.handler
import cc.trixey.invero.ui.bukkit.nms.persistContainerId
import cc.trixey.invero.ui.common.event.ClickType
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.submitAsync

/**
 * Invero
 * cc.trixey.invero.ui.bukkit.InventoryPacket
 *
 * @author Arasple
 * @since 2023/1/20 13:14
 */
class InventoryPacket(override val window: BukkitWindow) : ProxyBukkitInventory {

    override val hidePlayerInventory: Boolean
        get() = window.hidePlayerInventory

    override var containerId: Int = persistContainerId

    override fun isVirtual(): Boolean {
        return true
    }

    internal var closed: Boolean = true
    private var clickCallback: (slot: Int, type: ClickType) -> Boolean = { _, _ -> true }

    val windowItems = arrayOfNulls<ItemStack?>(containerType.entireWindowSize)

    fun updatePlayerItems(update: Boolean = false) {
        if (hidePlayerInventory) return

        windowItems.apply {
            viewer.copyStorage().forEachIndexed { index, itemStack ->
                val slot = containerSize + if (index < 9) index + 27 else index - 9
                this[slot] = itemStack
                if (update) update(slot)
            }
        }
    }

    fun onClick(handler: (slot: Int, type: ClickType) -> Boolean): InventoryPacket {
        clickCallback = handler
        return this
    }

    override fun clear(slots: Collection<Int>) {
        slots.forEach { set(it, null) }
    }

    fun update() {
        handler.sendWindowItems(viewer, persistContainerId, windowItems.toList())
    }

    fun update(vararg slot: Int) {
        slot.forEach {
            handler.sendWindowSetSlot(viewer, persistContainerId, it, windowItems[it])
        }
    }

    override fun get(slot: Int): ItemStack? {
        return windowItems[slot]
    }

    override fun set(slot: Int, itemStack: ItemStack?) {
        try {
            windowItems[slot] = itemStack
        } catch (e: Throwable) {
            println("Failed to set slot $slot to $itemStack")
        }
        update(slot)
    }

    override fun isViewing(): Boolean {
        return !closed
    }

    override fun open() {
        closed = false
        updatePlayerItems()
        handler.sendWindowOpen(viewer, persistContainerId, containerType, inventoryTitle)
        update()

        // temp
        if (!hidePlayerInventory) {
            submitAsync(delay = 10L, period = 20L) {
                if (!window.isViewing()) {
                    cancel()
                    return@submitAsync
                }
                updatePlayerItems(true)
            }
        }
    }

    fun handleClickEvent(slot: Int, type: ClickType) {
        if (!clickCallback(slot, type)) return
        if (type.isItemMoveable) update()
        val pos = window.scale.convertToPosition(slot)

        window.panels.sortedByDescending { it.weight }.forEach {
            if (pos in it.area) {
                if (it.runClickCallbacks(pos, type, null)) {
                    it.handleClick(pos - it.locate, type, null)
                }
                return
            }
        }
    }

}