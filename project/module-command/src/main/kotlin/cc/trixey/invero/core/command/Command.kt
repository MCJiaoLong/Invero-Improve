package cc.trixey.invero.core.command

import cc.trixey.invero.core.command.sub.CommandItem
import cc.trixey.invero.core.command.sub.CommandMenu
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand

/**
 * Invero
 * cc.trixey.invero.core.command.Command
 *
 * @author Arasple
 * @since 2023/1/19 11:36
 */
@CommandHeader(
    name = "invero",
    aliases = ["i"],
    permission = "invero.command.access",
    description = "Invero's main command"
)
object Command {

    @CommandBody
    val main = mainCommand { createHelper() }

    @CommandBody
    val dev = CommandDev

    @CommandBody
    val menu = CommandMenu

    @CommandBody
    val item = CommandItem

}