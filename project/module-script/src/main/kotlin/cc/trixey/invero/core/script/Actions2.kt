package cc.trixey.invero.core.script

import cc.trixey.invero.core.script.loader.InveroKetherParser
import taboolib.common.platform.function.console
import taboolib.module.kether.combinationParser

/**
 * Invero
 * cc.trixey.invero.core.script.kether.Actions2
 *
 * @author Arasple
 * @since 2023/2/26 18:01
 */
@InveroKetherParser(["chance"])
internal fun chance() = combinationParser {
    it.group(double()).apply(it) { random ->
        val chance = if (random > 1) random else random * 100.0
        now {
            return@now chance > (0..100).random()
        }
    }
}

@InveroKetherParser(["playerPerform"])
internal fun command() = combinationParser {
    it.group(action()).apply(it) { s ->
        now {
            newFrame(s).run<Any>().getNow(null)?.toString()?.let {
                player().performCommand(it)
            }
        }
    }
}

@InveroKetherParser(["console"])
internal fun actionConsole() = combinationParser {
    it.group(text()).apply(it) { s ->
        now {
            console().performCommand(parse(s))
        }
    }
}