package cc.trixey.invero.core.script

import cc.trixey.invero.common.Invero
import cc.trixey.invero.core.script.loader.InveroKetherParser
import org.bukkit.Bukkit
import taboolib.expansion.Database
import taboolib.module.kether.combinationParser

/**
 * Invero
 * cc.trixey.invero.core.script.kether.ActionPersistData
 *
 * @author Arasple
 * @since 2023/2/19 19:59
 */
object ActionPersistData {

    /*
    persist get <key> by global
    persist set <key> to <value> by global
     */
    @InveroKetherParser(["persist"])
    fun parserData() = combinationParser {
        it.group(
            // get/set/del
            symbol(),
            // key
            text(),
            // value
            command("to", then = action()).option().defaultsTo(null),
            // handler
            command("by", "player", then = action()).option().defaultsTo(null)
        ).apply(it) { action, tag, value, handle ->
            now {
                val source = handle?.let { it1 -> newFrame(it1).run<Any>().getNow("global").toString() }
                val isGlobalContainer: Boolean
                val dataContainer = if (source == null || source.equals("global", true)) {
                    isGlobalContainer = true
                    Invero.API.getDataManager().getGlobalData()
                } else {
                    isGlobalContainer = false
                    Invero.API.getDataManager().getPlayerData(
                        Bukkit.getPlayerExact(source) ?: player()
                    )
                }
                val key = "${if (isGlobalContainer) "global" else "player"}@$tag"


                when (action) {
                    "get" -> dataContainer[key]
                    "set" -> {
                        value
                            ?.let { it1 -> newFrame(it1).run<Any>().getNow(null) }
                            ?.let { v -> dataContainer[key] = v }
                    }

                    "del", "delete", "remove" -> {
                        dataContainer.source.remove(key)
                        dataContainer.database.removeBy(dataContainer.user, key)
                    }

                    else -> "<ERROR ACTION: $action>"
                }
            }
        }
    }

    fun Database.removeBy(user: String, name: String) {
        type.tableVar().delete(dataSource) {
            where("user" eq user and ("key" eq name))
        }
    }

}