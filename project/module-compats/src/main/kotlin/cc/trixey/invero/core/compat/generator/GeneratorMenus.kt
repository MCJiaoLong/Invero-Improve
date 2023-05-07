package cc.trixey.invero.core.compat.generator

import cc.trixey.invero.common.Invero
import cc.trixey.invero.common.sourceObject
import cc.trixey.invero.core.BaseMenu
import cc.trixey.invero.core.compat.DefGeneratorProvider
import cc.trixey.invero.core.generator.BaseGenerator

/**
 * Invero
 * cc.trixey.invero.core.compat.generator.GeneratorMenus
 *
 * @author Arasple
 * @since 2023/2/18 19:22
 */
@DefGeneratorProvider("menus")
class GeneratorMenus : BaseGenerator() {

    override fun generate() {
        generated = Invero.API.getMenuManager().getMenus().map {
            val menu = it as BaseMenu

            sourceObject {
                put("id", menu.id)
                put("type", menu.settings.containerType.name)
                put("rows", menu.settings.rows)
                put("virtual", menu.settings.virtual)
                put("panels", menu.panels.size)
            }
        }
    }

}