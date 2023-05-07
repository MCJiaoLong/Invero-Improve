package cc.trixey.invero.core.script

import cc.trixey.invero.core.BaseMenu
import cc.trixey.invero.core.script.loader.InveroKetherParser
import taboolib.common.platform.function.submitAsync
import taboolib.module.kether.combinationParser
import java.util.concurrent.CompletableFuture

/**
 * Invero
 * cc.trixey.invero.core.script.kether.ActionInvokeNode
 *
 * @author Arasple
 * @since 2023/2/2 14:14
 */
object ActionInvokeNode {

    @InveroKetherParser(["node"])
    fun node() = combinationParser {
        it.group(
            // 节点名称
            text(),
            // 参数
            command("with", then = action()).option().defaultsTo(null)
        ).apply(it) { nodeName, with ->
            future {
                val session = session()
                val menu = session?.menu as? BaseMenu ?: return@future CompletableFuture.completedFuture(false)
                val params = with
                    ?.let { w -> newFrame(w).run<Any>().getNow(null) }
                    .toParameter()
                    .let { strings -> mapOf("invokeArgs" to strings) }
                val node = menu.nodes?.get(nodeName)
                    ?: return@future CompletableFuture.completedFuture("<NODE: $nodeName> ${menu.nodes?.keys}")
                val result = node.invoke(session, params)

                CompletableFuture.completedFuture(result)
            }
        }
    }

    @InveroKetherParser(["task"])
    fun task() = combinationParser {
        it.group(
            text(),
            command("with", then = action()).option().defaultsTo(null)
        ).apply(it) { name, with ->
            future {
                val session = session()
                val menu = session?.menu as? BaseMenu ?: return@future CompletableFuture.completedFuture(false)
                val params = with
                    ?.let { w -> newFrame(w).run<Any>().getNow(null) }
                    .toParameter()
                    .let { strings -> mapOf("invokeArgs" to strings) }
                val task = menu.tasks?.get(name)
                    ?: return@future CompletableFuture.completedFuture("<TASK: $name> ${menu.nodes?.keys}")

                CompletableFuture.completedFuture(submitAsync {
                    task.run(session, params)
                })
            }
        }
    }

    private fun Any?.toParameter(): List<String> = when (this) {
        null -> emptyList()
        is Collection<*> -> map { it.toString() }
        else -> listOf(toString())
    }

}