package cc.trixey.invero.core.api

import cc.trixey.invero.common.api.InveroJavaScriptHandler
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.function.server
import taboolib.common5.compileJS
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import javax.script.CompiledScript
import javax.script.ScriptContext
import javax.script.SimpleBindings
import javax.script.SimpleScriptContext

/**
 * Invero
 * cc.trixey.invero.core.api.DefaultJavaScriptHandler
 *
 * @author Arasple
 * @since 2023/2/8 10:57
 */
class DefaultJavaScriptHandler : InveroJavaScriptHandler {

    private val cacheCompiled = ConcurrentHashMap<String, CompiledScript>()
    private val persistBindings = ConcurrentHashMap<String, Any>(
        buildMap {
            put("server", server())
            put("Utils", JavaScriptUtils())
        }
    )
    private val persistFunctions = ConcurrentHashMap<String, Function<Any, Any>>()

    override fun runScript(
        script: String,
        cache: Boolean,
        bindings: Map<String, Any?>,
        block: SimpleScriptContext.() -> Unit
    ): Any? {
        val context = SimpleScriptContext()
        context.setBindings(
            SimpleBindings(persistBindings).also { it += bindings },
            ScriptContext.ENGINE_SCOPE
        )
        persistFunctions.forEach { (key, value) -> context.setAttribute(key, value, ScriptContext.ENGINE_SCOPE) }

        block(context)

        val compiledScript =
            if (cache) cacheCompiled.computeIfAbsent(script) { it.compileJS()!! }
            else script.compileJS()!!

        return compiledScript.eval(context)
    }

    override fun registerPersistFunction(name: String, function: Function<Any, Any>) {
        persistFunctions[name] = function
    }

    override fun registerPersistBindings(bindings: Map<String, Any>) {
        persistBindings += bindings
    }

    companion object {

        @Awake(LifeCycle.INIT)
        fun init() {
            PlatformFactory.registerAPI<InveroJavaScriptHandler>(DefaultJavaScriptHandler())
        }

    }

}