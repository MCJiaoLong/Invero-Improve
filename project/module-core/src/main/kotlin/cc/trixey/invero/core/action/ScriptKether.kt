package cc.trixey.invero.core.action

import cc.trixey.invero.core.Context
import cc.trixey.invero.core.serialize.ScriptKetherSerializer
import cc.trixey.invero.core.util.KetherHandler
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

/**
 * Invero
 * cc.trixey.invero.core.action.ScriptKether
 *
 * @author Arasple
 * @since 2023/1/16 13:11
 */
@Serializable(with = ScriptKetherSerializer::class)
class ScriptKether(val script: String) : Condition {

    override var default: Boolean = true

    private val staticCondition: Pair<Boolean, CompletableFuture<Any?>>

    init {
        val isTrue =
            script == "1" || script.equals("true", ignoreCase = true) || script.equals("yes", ignoreCase = true);

        val isFalse =
            script == "0" || script.equals("false", ignoreCase = true) || script.equals("no", ignoreCase = true);

        staticCondition = (isTrue || isFalse) to CompletableFuture.completedFuture(isTrue)
    }


    override fun invoke(context: Context): CompletableFuture<Any?> {
        if (staticCondition.first) return staticCondition.second
        val player = context.player

        return KetherHandler.invoke(script, player, context.variables)
    }

}