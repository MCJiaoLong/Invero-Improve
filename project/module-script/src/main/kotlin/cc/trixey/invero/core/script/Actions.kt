package cc.trixey.invero.core.script

import cc.trixey.invero.core.Context
import cc.trixey.invero.core.compat.bungeecord.Bungees
import cc.trixey.invero.core.compat.eco.HookPlayerPoints
import cc.trixey.invero.core.script.loader.InveroKetherParser
import cc.trixey.invero.core.util.translateFormattedMessage
import org.bukkit.entity.Player
import taboolib.common5.cdouble
import taboolib.module.kether.*
import taboolib.platform.compat.depositBalance
import taboolib.platform.compat.getBalance
import taboolib.platform.compat.withdrawBalance
import taboolib.platform.util.onlinePlayers
import java.util.concurrent.CompletableFuture

/**
 * Invero
 * cc.trixey.invero.core.script.Actions
 *
 * @author Arasple
 * @since 2023/1/24 22:51
 */
@InveroKetherParser(["parse"])
internal fun actionParser() = scriptParser {
    val str = it.nextParsedAction()
    actionTake {
        run(str).str { s ->
            val context = contextVar<Context?>("@context")?.variables ?: variables().toMap()
            val player = player()

            s.translateFormattedMessage(player, context)
        }
    }
}

/*
connect <serverName> for <playerName>
 */
@InveroKetherParser(["connect", "bungee"])
internal fun actionConnect() = combinationParser {
    it.group(
        text(), command("for", then = action()).option().defaultsTo(null)
    ).apply(it) { server, player ->
        future {
            if (player == null) {
                session()?.viewer?.get<Player>()?.let { player ->
                    CompletableFuture.completedFuture(Bungees.connect(player, server))
                } ?: CompletableFuture.completedFuture(null)
            } else {
                newFrame(player).run<Any>().thenApply { playerId ->
                    onlinePlayers.find { p -> p.name == playerId }?.let { p -> Bungees.connect(p, server) }
                }
            }
        }
    }
}

/**
 * eco
 * eco get
 * eco take 200
 * eco give 200
 * eco set 200
 */
//@InveroKetherParser(["eco", "money", "vault"])
@KetherParser(["eco", "money", "vault"], shared = true)
internal fun actionEco() = scriptParser { it ->
    if (!it.hasNext()) actionNow { player().getBalance() }
    else {
        val method = it.nextToken()
        if (method == null || method == "get" || method == "balance") actionNow { player().getBalance() }
        else {
            val money = it.nextToken()/*.translateFormattedMessage(player,context)*/.cdouble
            when (method) {
                "has" -> actionNow { player().getBalance() >= money }
                "take" -> actionNow { player().withdrawBalance(money) }
                "give" -> actionNow { player().depositBalance(money) }
                else -> error("Unknown eco method: $method")
            }
        }
    }
}

@InveroKetherParser(["playerpoints", "points"])
internal fun actionPoints() = scriptParser {
    if (!it.hasNext()) actionNow { HookPlayerPoints.look(player()) }
    else {
        val method = it.nextToken()
        if (method == null || method == "get" || method == "balance") actionNow {
            HookPlayerPoints.look(player()) ?: -1
        }
        else {
            val points = it.nextInt()
            when (method) {
                "has" -> actionNow { (HookPlayerPoints.look(player()) ?: 0) >= points }
                "take" -> actionNow { HookPlayerPoints.take(player(), points) }
                "give" -> actionNow { HookPlayerPoints.add(player(), points) }
                else -> error("Unknown playerpoints method: $method")
            }
        }
    }
}