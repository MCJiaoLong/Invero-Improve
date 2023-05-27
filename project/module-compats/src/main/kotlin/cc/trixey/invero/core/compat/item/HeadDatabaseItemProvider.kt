package cc.trixey.invero.core.compat.item

import cc.trixey.invero.common.ItemSourceProvider
import cc.trixey.invero.core.compat.DefItemProvider
import cc.trixey.invero.core.compat.PluginHook
import me.arcaniax.hdb.api.HeadDatabaseAPI
import org.bukkit.inventory.ItemStack

/**
 * Invero
 * cc.trixey.invero.core.compat.item.HeadDatabaseItemProvider
 *
 * @author Arasple
 * @since 2023/1/29 15:52
 */
@DefItemProvider(["headdatabase", "hdb"])
class HeadDatabaseItemProvider : ItemSourceProvider, PluginHook() {

    override val pluginName = "HeadDatabase"

    private val headDatabaseAPI by lazy { HeadDatabaseAPI() }

    override fun translateIdentifier(): Boolean {
        return false
    }

    override fun getItem(identifier: String, context: Any?): ItemStack? {
        return headDatabaseAPI.getItemHead(identifier) ?: headDatabaseAPI.randomHead
    }

}