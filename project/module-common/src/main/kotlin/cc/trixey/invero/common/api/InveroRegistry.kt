package cc.trixey.invero.common.api

import cc.trixey.invero.common.*
import kotlinx.serialization.json.JsonElement
import org.bukkit.entity.Player

/**
 * Invero
 * cc.trixey.invero.common.api.InveroRegistry
 *
 * @author Arasple
 * @since 2023/2/25 14:15
 */
interface InveroRegistry {

    fun registerElementGenerator(namespace: String, id: String, provider: ElementGenerator)

    fun registerItemSourceProvider(name: String, provider: ItemSourceProvider)

    fun callActivator(player: Player, name: String, vararg params: Any): Boolean

    fun registerAction(name: String, action: MenuAction)

    fun createElementGenerator(identifier: String): ElementGenerator?

    fun registerActivator(name: String, activator: MenuActivator<*>)

    fun createActivator(menu: Menu, name: String, jsonElement: JsonElement): MenuActivator<*>?

    fun getItemSourceProvider(name: String): ItemSourceProvider?


}