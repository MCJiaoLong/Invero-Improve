package cc.trixey.invero.ui.bukkit.util

import org.bukkit.NamespacedKey
import taboolib.platform.util.bukkitPlugin

/**
 * Invero
 * cc.trixey.invero.ui.bukkit.util.PDC
 *
 * @author Arasple
 * @since 2023/2/26 18:14
 */
val String.asNamespacedKey: NamespacedKey
    get() = NamespacedKey(bukkitPlugin, this)
