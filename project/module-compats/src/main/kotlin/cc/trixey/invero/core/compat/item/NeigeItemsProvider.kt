package cc.trixey.invero.core.compat.item

import cc.trixey.invero.common.ItemSourceProvider
import cc.trixey.invero.core.Context
import cc.trixey.invero.core.compat.DefItemProvider
import cc.trixey.invero.core.compat.PluginHook
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import pers.neige.neigeitems.manager.HookerManager.mythicMobsHooker
import pers.neige.neigeitems.manager.ItemManager

/**
 * Invero
 * cc.trixey.invero.core.compat.item.NeigeItemsProvider
 *
 * @author Neige
 * @since 2023/2/24 16:15
 */
@DefItemProvider(["neigeitems", "ni"])
class NeigeItemsProvider : ItemSourceProvider, PluginHook() {

    override val pluginName = "NeigeItems"

    // identifier可以直接填id, 也可以填"id data", data代表指向数据, 指向数据和id之间用空格分离
    // 指向数据是用来控制生成指定形态物品的
    override fun getItem(identifier: String, context: Any?): ItemStack? {
        with(identifier.split(" ", limit = 2)) {
            // 物品ID
            val id = get(0)
            // 解析对象
            val player = (context as? Context)?.viewer?.get<Player>()
            // 指向数据
            val data = getOrNull(1)

            // 尝试返回NI物品, 没有就尝试返回MM物品
            return ItemManager.getItemStack(id, player, data) ?: mythicMobsHooker?.getItemStack(id)
        }
    }

}