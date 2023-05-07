package cc.trixey.invero.core.util

import cc.trixey.invero.core.icon.Slot
import cc.trixey.invero.ui.common.Pos
import cc.trixey.invero.ui.common.Scale

/**
 * Invero
 * cc.trixey.invero.core.util.BaseMenu
 *
 * @author Arasple
 * @since 2023/1/16 12:52
 */
fun List<Slot>.flatRelease(scale: Scale): List<Pos> {
    return flatMap { it.release(scale) }
}