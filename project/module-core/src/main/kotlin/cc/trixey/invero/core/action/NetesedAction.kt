package cc.trixey.invero.core.action

import cc.trixey.invero.core.Context
import cc.trixey.invero.core.serialize.NetesedActionSerializer
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

/**
 * Invero
 * cc.trixey.invero.core.action.NetesedAction
 *
 * @author Arasple
 * @since 2023/1/18 12:14
 */
@Serializable(with = NetesedActionSerializer::class)
class NetesedAction(val actions: List<Action>) : Action() {

    override fun run(context: Context): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        fun runAction(index: Int) {
            actions[index].run(context).thenAccept {
                if (it && index < actions.lastIndex) {
                    runAction(index + 1)
                } else {
                    future.complete(it)
                }
            }
        }

        runAction(0)

        return future
    }

}