package cc.trixey.invero.core.script

import cc.trixey.invero.core.script.loader.InveroKetherParser
import taboolib.module.kether.combinationParser

/**
 * Invero
 * cc.trixey.invero.core.script.kether.ActionSource
 *
 * @author Arasple
 * @since 2023/1/29 22:47
 */
object ActionSource {

    @InveroKetherParser(["element"])
    fun parser() = combinationParser {
        it.group(
            text().option()
        ).apply(it) { key ->
            now {
                selfSourceObject().let { obj ->
                    if (key != null) obj[key]
                    else obj.content
                }
            }
        }
    }

}