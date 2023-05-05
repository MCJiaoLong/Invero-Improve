package cc.trixey.invero.ui.bukkit

import cc.trixey.invero.ui.bukkit.api.dsl.viewer
import cc.trixey.invero.ui.bukkit.api.findWindow
import cc.trixey.invero.ui.bukkit.nms.persistContainerId
import cc.trixey.invero.ui.bukkit.nms.sendCancelCoursor
import cc.trixey.invero.ui.bukkit.util.copyUIMarked
import cc.trixey.invero.ui.common.event.ClickType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.nms.MinecraftVersion.isUniversal
import taboolib.module.nms.PacketReceiveEvent

/**
 * Invero
 * cc.trixey.invero.ui.bukkit.Listener
 *
 * @author Arasple
 * @since 2023/1/12 18:03
 */
object Listener {

    private val FIELD_CONTAINER_ID = if (isUniversal) "containerId" else "id"
    private val FILEDS_WINDOW_CLICK =
        if (isUniversal) arrayOf("containerId", "slotNum", "buttonNum", "clickType", "carriedItem")
        else arrayOf("a", "slot", "button", "shift", "chemdah")

    @Awake(LifeCycle.ACTIVE)
    fun init() {
        // init NBT handle
        ItemStack(Material.DIAMOND).copyUIMarked("Invero", 0)
    }

    @SubscribeEvent
    fun e(e: InventoryClickEvent) = e.delegatedEvent {
        when (e.action) {
            InventoryAction.MOVE_TO_OTHER_INVENTORY -> handleItemsMove(e)
            InventoryAction.COLLECT_TO_CURSOR -> handleItemsCollect(e)
            else -> handleClick(e)
        }
    }

    @SubscribeEvent
    fun e(e: InventoryDragEvent) = e.delegatedEvent { handleDrag(e) }

    @SubscribeEvent
    fun e(e: InventoryOpenEvent) {
        if (!e.delegatedEvent { handleOpenEvent(e) }) {
            (e.player as Player).updateInventory()
        }
    }

    @SubscribeEvent
    fun e(e: InventoryCloseEvent) = e.delegatedEvent { handleCloseEvent(e) }

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) = e.player.viewer.viewingWindow()?.close(false, updateInventory = false)

    private fun InventoryEvent.delegatedEvent(block: InventoryVanilla.() -> Unit) = view.topInventory.let {
        val holder = it.holder
        if (holder is InventoryVanilla.Holder) {
            (holder.window.inventory as InventoryVanilla).block()
            return@let true
        }
        return@let false
    }

    @SubscribeEvent
    fun e(e: PacketReceiveEvent) {
        val player = e.player
        val viewer = player.viewer
        val packet = e.packet

        when (packet.name) {
            "PacketPlayInCloseWindow" -> {
                val id = packet.read<Int>(FIELD_CONTAINER_ID) ?: return
                if (id == persistContainerId) {
                    val window = viewer.viewingPacketWindow() ?: return
                    submit { window.close(doCloseInventory = false, updateInventory = true) }
                }
            }

            "PacketPlayInWindowClick" -> {
                val inventory = viewer.viewingWindow()?.inventory ?: return

                if (inventory is InventoryVanilla && inventory.hidePlayerInventory) {
                    player.sendCancelCoursor()
                    submit { inventory.updatePlayerInventory() }
                    return
                } else {
                    packet.read<Int>(FILEDS_WINDOW_CLICK[0]).let { if (it != persistContainerId) return }
                    inventory as InventoryPacket
                }

                val rawSlot = packet.read<Int>(FILEDS_WINDOW_CLICK[1]) ?: return
                val button = packet.read<Int>(FILEDS_WINDOW_CLICK[2]) ?: return
                val mode = ClickType.Mode.valueOf(packet.read<Any>(FILEDS_WINDOW_CLICK[3]).toString())
                val type = ClickType.find(mode, button, rawSlot) ?: return


                if (rawSlot >= 0) {
                    player.sendCancelCoursor()
                    inventory.update(rawSlot)
                }

                submit {
                    inventory.handleClickEvent(rawSlot, type)
                }
            }
        }
    }

    /*
    ENTITIY_SAFETY
     */
    @SubscribeEvent
    fun e(e: PlayerDeathEvent) = e.entity.windowClosure()

    @SubscribeEvent
    fun e(e: PlayerChangedWorldEvent) = e.player.windowClosure()

//    @SubscribeEvent
//    fun e(e: PlayerJoinEvent) {
//        val player = e.player
//        if (MinecraftVersion.majorLegacy > 11400) {
//            val byteInventory = player
//                .persistentDataContainer
//                .get("invero_restore".asNamespacedKey, PersistentDataType.BYTE_ARRAY)
//
//            submitAsync {
//                byteInventory?.deserializeToInventory(player.inventory).also {
//                    info("Restored player inventory for ${player.name}")
//                }
//            }
//        }
//    }

    private fun PlayerViewer.viewingPacketWindow(): BukkitWindow? {
        return viewingWindow()?.let { if (it.inventory is InventoryPacket) it else null }
    }

    private fun PlayerViewer.viewingWindow(): BukkitWindow? {
        return findWindow(name)
    }

    private fun Player.windowClosure() {
        findWindow(name)?.close(doCloseInventory = false, updateInventory = true)
    }

}