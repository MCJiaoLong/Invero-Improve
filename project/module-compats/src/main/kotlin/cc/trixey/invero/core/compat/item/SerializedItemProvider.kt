package cc.trixey.invero.core.compat.item

import cc.trixey.invero.common.ItemSourceProvider
import cc.trixey.invero.common.util.standardJson
import cc.trixey.invero.core.compat.DefItemProvider
import cc.trixey.invero.core.serialize.ItemStackJsonSerializer
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.deserializeToItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Invero
 * cc.trixey.invero.core.compat.item.SerializedItemProvider
 *
 * @author Arasple
 * @since 2023/1/29 15:45
 */
@DefItemProvider(["base64", "json", "serialized"])
class SerializedItemProvider : ItemSourceProvider {

    companion object {

        private val cache = ConcurrentHashMap<String, ItemStack>()

    }

    override fun getItem(identifier: String, context: Any?): ItemStack {
        return cache.computeIfAbsent(identifier) {
            if (identifier.startsWith("{"))
                standardJson.decodeFromString(ItemStackJsonSerializer, identifier)
            else
                Base64.getDecoder().decode(identifier).deserializeToItemStack()
        }.clone()
    }

}