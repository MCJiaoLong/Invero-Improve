package cc.trixey.invero.core.compat.activators

import cc.trixey.invero.common.Invero
import cc.trixey.invero.common.MenuActivator
import cc.trixey.invero.common.chemdah.InferItem
import cc.trixey.invero.common.chemdah.InferItem.Companion.toInferItem
import cc.trixey.invero.core.compat.DefActivator
import cc.trixey.invero.core.util.session
import cc.trixey.invero.ui.bukkit.util.proceed
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.MinecraftVersion
import taboolib.platform.util.isOffhand

/**
 * Invero
 * cc.trixey.invero.core.compat.activators.ActivatorItem
 *
 * @author Arasple
 * @since 2023/2/25 15:06
 */
@DefActivator(["item", "items"])
class ActivatorItem(private val inferItem: InferItem, private val raw: JsonElement) : MenuActivator<ActivatorItem>() {

    constructor() : this(InferItem(emptyList()), JsonPrimitive(0))

    companion object {

        @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun e(e: PlayerInteractEvent) {
            if (MinecraftVersion.majorLegacy >= 10900 && e.isOffhand()) return
            if (e.player.session != null) return
            val item = e.item ?: return

            Invero.API.getRegistry().callActivator(e.player, "ITEM", item).proceed {
                e.isCancelled = true
            }
        }

    }

    override fun call(player: Player, vararg params: Any): Boolean {
        val itemStack = params[0] as ItemStack
        if (inferItem.isItem(itemStack)) {
            activate(player, buildMap {
                put("activator_item", itemStack)
            })
            return true
        }
        return false
    }

    override fun deserialize(element: JsonElement): ActivatorItem {
        when (element) {
            is JsonPrimitive -> listOf(element.content).toInferItem()
            is JsonArray -> InferItem(element.map { it.jsonPrimitive.content.toInferItem() })
            else -> error("Unsupported type $element")
        }.let {
            return ActivatorItem(it, element)
        }
    }

    override fun serialize(activator: ActivatorItem) = activator.raw

}