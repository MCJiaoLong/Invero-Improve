package cc.trixey.invero.core.item

import cc.trixey.invero.core.Context
import cc.trixey.invero.ui.bukkit.util.requestHead
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem

/**
 * Invero
 * cc.trixey.invero.core.item.TextureHead
 *
 * @author Arasple
 * @since 2023/1/16 10:36
 */
@Serializable
class TextureHead(@SerialName("head") override val raw: String) : Texture() {

    @Transient
    private val defaultHead = buildItem(XMaterial.PLAYER_HEAD) { name = "§8..." }

    @Transient
    override var lazyTexture: ItemStack? = null

    override fun generateItem(context: Context, block: (ItemStack) -> Unit) {
        if (lazyTexture != null) return block(lazyTexture!!)
        if (!containsPlaceholder && raw.length > 20) requestHead(raw.trim()) { lazyTexture = it.also(block) }
        else {
            val parsed = context.parse(raw)
            requestHead(parsed) { block(it.clone()) }
        }
    }

    override fun clone() = TextureHead(raw)

}