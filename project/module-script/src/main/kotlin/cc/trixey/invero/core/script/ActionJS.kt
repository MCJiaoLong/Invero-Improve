package cc.trixey.invero.core.script

import cc.trixey.invero.core.script.loader.InveroKetherParser
import cc.trixey.invero.core.util.runJS
import taboolib.module.kether.combinationParser
import taboolib.module.kether.deepVars

/**
 * Invero
 * cc.trixey.invero.core.script.kether.ActionJS
 *
 * @author Arasple
 * @since 2023/2/8 11:09
 */
object ActionJS {

    @InveroKetherParser(["$", "js", "javascript"])
    fun parser() = combinationParser {
        it.group(symbol()).apply(it) { script ->
            future { runJS(script, session(), deepVars().filterNot { v -> v.key.startsWith("@") }) }
        }
    }

}