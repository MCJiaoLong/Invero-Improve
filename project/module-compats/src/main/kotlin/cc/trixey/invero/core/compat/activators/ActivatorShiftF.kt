package cc.trixey.invero.core.compat.activators

import cc.trixey.invero.common.Invero
import cc.trixey.invero.common.MenuActivator
import cc.trixey.invero.core.compat.DefActivator
import cc.trixey.invero.ui.bukkit.util.proceed
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.clong
import java.util.concurrent.ConcurrentHashMap

/**
 * Invero
 * cc.trixey.invero.core.compat.activators.ActivatorBrevis
 *
 * @author Arasple
 * @since 2023/2/26 14:04
 */
@DefActivator(["shift_f"])
class ActivatorShiftF(private val interval: Long = 8_00) : MenuActivator<ActivatorShiftF>() {

    companion object {

        private val lastSneak = ConcurrentHashMap<String, Long>()

        @SubscribeEvent
        fun e(e: PlayerToggleSneakEvent) {
            lastSneak[e.player.name] = System.currentTimeMillis()
        }

        @SubscribeEvent
        fun e(e: PlayerSwapHandItemsEvent) {
            lastSneak.remove(e.player.name)?.let {
                Invero.API.getRegistry().callActivator(e.player, "SHIFT_F", System.currentTimeMillis() - it).proceed {
                    e.isCancelled = true
                }
            }
        }

    }

    override fun call(player: Player, vararg params: Any): Boolean {
        val pass = params[0] as Long
        return if (pass < interval) {
            activate(player)
            true
        } else {
            false
        }
    }

    override fun deserialize(element: JsonElement): ActivatorShiftF {
        return ActivatorShiftF(element.jsonPrimitive.contentOrNull.clong.coerceAtLeast(50))
    }

    override fun serialize(activator: ActivatorShiftF): JsonElement {
        return JsonPrimitive(activator.interval)
    }

}