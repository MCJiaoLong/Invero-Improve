package cc.trixey.invero.ui.bukkit.util

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import taboolib.module.nms.ItemTag
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.getItemTag
import taboolib.module.nms.setItemTag
import taboolib.platform.util.*

private val namespacedKey = "invero_icon".asNamespacedKey

fun ItemStack.isUIMarked(): Boolean {
    var isMarked = false
    if (MinecraftVersion.majorLegacy > 11400) {
        modifyMeta<ItemMeta> {
            isMarked = persistentDataContainer.get(namespacedKey, PersistentDataType.STRING) != null
        }
    } else {
        isMarked = getItemTag().containsKey("invero")
    }
    return isMarked
}

fun ItemStack.copyUIMarked(viewer: String, slot: Int): ItemStack {
    if (isAir) return this
    val mark = "$viewer:$slot"
    val modified = clone()

    return if (MinecraftVersion.majorLegacy > 11400) {
        modified.modifyMeta<ItemMeta> {
            persistentDataContainer.set(namespacedKey, PersistentDataType.STRING, mark)
        }
    } else {
        ItemTag().apply {
            putAll(getItemTag())
            put("invero", mark)
        }.let { modified.setItemTag(it) }
    }
}

fun randomItem(builder: ItemBuilder.() -> Unit = {}): ItemStack {
    var itemStack: ItemStack? = null
    while (itemStack.isAir()) {
        itemStack = ItemStack(Material.values().filter { it.isItem || it.isBlock }.random())
    }
    return buildItem(itemStack!!, builder).also {
        it.amount = it.amount.coerceAtMost(it.maxStackSize)
    }
}

fun randomMaterial(): Material {
    return Material.values().filter { (it.isItem || it.isBlock) && it.isNotAir() }.random()
}