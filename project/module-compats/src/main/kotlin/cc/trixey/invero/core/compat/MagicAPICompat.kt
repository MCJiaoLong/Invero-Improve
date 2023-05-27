package cc.trixey.invero.core.compat

import cc.trixey.invero.common.Invero
import com.francobm.magicosmetics.api.MagicAPI
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

/**
 * Invero
 * cc.trixey.invero.core.compat.MagicAPICompat
 *
 * @author Arasple
 * @since 2023/5/4 10:42
 */
object MagicAPICompat {

    @Awake(LifeCycle.LOAD)
    fun init() {
        // 检测插件
        if (Bukkit.getPluginManager().getPlugin("MagicCosmetics") == null) {
            return
        }
        Invero.API.getJavaScriptHandler().registerPersistBindings(mapOf("MagicAPI" to MagicAPI()))
    }

}