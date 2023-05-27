package cc.trixey.invero.core.script

import cc.trixey.invero.core.icon.IconElement
import cc.trixey.invero.core.script.loader.InveroKetherParser
import cc.trixey.invero.ui.common.Panel
import cc.trixey.invero.ui.common.panel.ElementalPanel
import cc.trixey.invero.ui.common.panel.TypedPanelContainer
import taboolib.module.kether.combinationParser

/**
 * Invero
 * cc.trixey.invero.core.script.kether.ActionIcon
 *
 * @author Arasple
 * @since 2023/1/21 19:47
 */
object ActionIcon {

    /*
    icon [by <id>] [at <slot>] update/relocate/item

    refresh
    sub_index
    pause_update
    pause_relocate
    pause_frames
    resume_update
    resume_relocate
    resume_frames
     */

    @InveroKetherParser(["icon"])
    fun parser() = combinationParser {
        it.group(
            command("by", then = text()).option().defaultsTo(null),
            command("at", then = int()).option().defaultsTo(-1),
            symbol(),
        ).apply(it) { by, at, action ->
            now {
                iconElementBy(by, at, null).apply {
                    if (action == "item") return@now itemStack
                    else handle(action)
                }
            }
        }
    }

    @InveroKetherParser(["icons"])
    fun parserIcons() = combinationParser { it ->

        fun updatePanel(panel: Panel, action: String) {
            when (panel) {
                is TypedPanelContainer<*> -> {
                    panel.panels.filter { panel.isPanelValid(it) }.forEach { updatePanel(it, action) }
                }

                is ElementalPanel -> {
                    panel.elements.value.keys.forEach {
                        if (it is IconElement) it.handle(action)
                    }
                }
            }
        }

        it.group(symbol()).apply(it) { action ->
            now { getRootPanels().forEach { updatePanel(it, action) } }
        }
    }

    private fun IconElement.handle(action: String) {
        when (action) {
            "relocate" -> relocate()
            "update" -> renderItem()
            "refresh" -> {
                relocate()
                renderItem()
            }

            "index", "sub_index" -> iconIndex
            "pause_update" -> pauseUpdateTask()
            "pause_relocate" -> pauseRelocateTask()
            "pause_frames" -> pauseFramesTask()
            "resume_update" -> resumeUpdateTask()
            "resume_relocate" -> resumeRelocateTask()
            "resume_frames" -> resumeFramesTask()
            else -> error("Unsupported action for icon: $action")
        }
    }

}