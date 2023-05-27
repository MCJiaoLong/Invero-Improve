package cc.trixey.invero.ui.bukkit

import cc.trixey.invero.ui.bukkit.util.isUIMarked
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import taboolib.common.io.unzip
import taboolib.common.io.zip
import taboolib.platform.util.isNotAir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Invero
 * cc.trixey.invero.ui.bukkit.PlayerStorage
 *
 * @author Arasple
 * @since 2023/1/20 17:16
 */
val storageMap = ConcurrentHashMap<UUID, Storage>()

fun Player.copyStorage(): Array<ItemStack?> {
    val from = inventory.storageContents.clone()
    val to = arrayOfNulls<ItemStack?>(36)
    from.forEachIndexed { it, itemStack ->
        to[it] = itemStack
    }
    return to
}

fun Player.isCurrentlyStored(): Boolean {
    return storageMap.containsKey(this.uniqueId)
}

fun Player.storePlayerInventory(hidePlayerInventory: Boolean) {
    if (storageMap.containsKey(uniqueId)) error("Player $name is already stored!")

    storageMap[uniqueId] = Storage(hidePlayerInventory, this)
}

fun Player.tempRestore(): ByteArray? {
    return storageMap[uniqueId]?.let { backup ->
        if (!backup.isPlayerInventoryHided) {
            for (i in 0..35) {
                val currentSlot = inventory.storageContents.getOrNull(i)
                val previousSlot = backup.storage.getOrNull(i)

                // 当前背包槽位不为 UI 图标 时
                if (currentSlot?.isUIMarked() != true) {
                    if (currentSlot.isNotAir()) {
                        backup.storage[i] = currentSlot
                    } else if (previousSlot.isNotAir()) {
                        backup.storage[i] = null
                    }
                }
            }
        }

        backup.storage.serializeToByteArray()
    }
}

fun ByteArray.deserializeToArrayItemStack(zipped: Boolean = true): Array<ItemStack?> {
    ByteArrayInputStream(if (zipped) unzip() else this).use { byteArrayInputStream ->
        BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream ->
            val items = bukkitObjectInputStream.readObject() as List<*>
            val size = bukkitObjectInputStream.readInt()
            val inv = arrayOfNulls<ItemStack?>(size)
            items.forEach { inv[it as Int] = bukkitObjectInputStream.readObject() as ItemStack }
            return inv
        }
    }
}

fun Array<ItemStack?>.serializeToByteArray(size: Int = this.size, zipped: Boolean = true): ByteArray {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
            val items = (0 until size).map { it to this[it] }.filter { it.second.isNotAir() }.toMap()
            bukkitObjectOutputStream.writeObject(items.keys.toList())
            bukkitObjectOutputStream.writeInt(size)
            items.forEach { (_, v) -> bukkitObjectOutputStream.writeObject(v) }
            val bytes = byteArrayOutputStream.toByteArray()
            return if (zipped) bytes.zip() else bytes
        }
    }
}

fun Player.restorePlayerInventory() = storageMap[uniqueId]?.let { backup ->

    if (!backup.isPlayerInventoryHided) {
        for (i in 0..35) {
            val currentSlot = inventory.storageContents.getOrNull(i)
            val previousSlot = backup.storage.getOrNull(i)

            // 当前背包槽位不为 UI 图标 时
            if (currentSlot?.isUIMarked() != true) {
                if (currentSlot.isNotAir()) {
                    backup.storage[i] = currentSlot
                } else if (previousSlot.isNotAir()) {
                    backup.storage[i] = null
                }
            }
        }
    }

    inventory.storageContents = backup.storage
    storageMap.remove(uniqueId)
}

class Storage(val isPlayerInventoryHided: Boolean, var storage: Array<ItemStack?> = arrayOfNulls(36)) {

    constructor(hidePlayerInventory: Boolean, player: Player) : this(
        hidePlayerInventory,
        player.inventory.storageContents.clone()
    )

}