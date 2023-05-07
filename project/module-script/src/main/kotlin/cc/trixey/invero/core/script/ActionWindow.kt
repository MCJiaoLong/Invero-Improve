package cc.trixey.invero.core.script

import cc.trixey.invero.core.script.loader.InveroKetherParser
import cc.trixey.invero.ui.bukkit.WindowAnvil
import taboolib.module.kether.combinationParser

/**
 * Invero
 * cc.trixey.invero.core.script.kether.ActionWindow
 *
 * @author Arasple
 * @since 2023/2/24 13:45
 */
object ActionWindow {

    /*
    window repairCost 1
     */
    @InveroKetherParser(["window"])
    fun parser() = combinationParser {
        it.group(
            symbol(),
            int(),
        ).apply(it) { _, value ->
            now {
                (session()?.window as? WindowAnvil)?.setRepairCost(value)
            }
        }
    }

}