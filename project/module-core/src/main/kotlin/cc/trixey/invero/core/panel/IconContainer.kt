package cc.trixey.invero.core.panel

import cc.trixey.invero.core.icon.Icon
import taboolib.library.reflex.Reflex.Companion.setProperty

/**
 * Invero
 * cc.trixey.invero.core.panel.IconContainer
 *
 * @author Arasple
 * @since 2023/1/30 14:47
 */
interface IconContainer {

    val icons: Map<String, Icon>

    fun registerIcons() {
        icons.forEach { (name, icon) -> registerIcon(icon, name) }
    }

    fun registerIcon(icon: Icon, name: String) {
        if (icon.id == null) icon.id = name
        icon.subIcons?.forEach {
            it.parent = icon
            it.id = icon.id
            if (it.inherit != false) {
                it.defaultFrame.inherit(icon.defaultFrame)
                if (it.frames == null) it.setProperty("frames", icon.frames)
                if (it.framesProperties == null) it.setProperty("framesProperties", icon.framesProperties)
                if (it.handler == null) it.setProperty("handler", icon.handler)
            }
        }
    }

}