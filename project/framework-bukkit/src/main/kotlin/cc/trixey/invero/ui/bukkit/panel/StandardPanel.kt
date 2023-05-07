package cc.trixey.invero.ui.bukkit.panel

import cc.trixey.invero.ui.bukkit.BukkitPanel
import cc.trixey.invero.ui.bukkit.PanelContainer
import cc.trixey.invero.ui.bukkit.element.Clickable
import cc.trixey.invero.ui.common.Elements
import cc.trixey.invero.ui.common.Pos
import cc.trixey.invero.ui.common.Scale
import cc.trixey.invero.ui.common.event.ClickType
import cc.trixey.invero.ui.common.panel.ElementalPanel
import cc.trixey.invero.ui.common.panel.PanelWeight
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * Invero
 * cc.trixey.invero.ui.bukkit.panel.StandardPanel
 *
 * @author Arasple
 * @since 2022/12/22 20:32
 */
open class StandardPanel(
    parent: PanelContainer,
    weight: PanelWeight,
    scale: Scale,
    locate: Pos
) : BukkitPanel(parent, weight, scale, locate), ElementalPanel {

    override val elements = Elements()

    override fun handleClick(pos: Pos, clickType: ClickType, e: InventoryClickEvent?): Boolean {
        elements.findElement(pos)?.let {
            if (it is Clickable<*>) {
                it.runClickCallbacks(clickType, e)
                return true
            }
        }
        return false
    }

}