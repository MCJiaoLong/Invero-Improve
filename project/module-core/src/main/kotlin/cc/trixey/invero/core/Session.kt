package cc.trixey.invero.core

import cc.trixey.invero.common.Invero
import cc.trixey.invero.common.Menu
import cc.trixey.invero.common.TaskGroup
import cc.trixey.invero.core.Session.Companion.VarType.*
import cc.trixey.invero.core.util.session
import cc.trixey.invero.core.util.translateFormattedMessage
import cc.trixey.invero.ui.bukkit.BukkitWindow
import cc.trixey.invero.ui.bukkit.PlayerViewer
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.expansion.getDataContainer
import java.util.concurrent.ConcurrentHashMap

/**
 * Invero
 * cc.trixey.invero.common.Session
 *
 * @author Arasple
 * @since 2023/1/15 22:40
 */
class Session(
    val viewer: PlayerViewer,
    val menu: Menu,
    val window: BukkitWindow,
    variables: Map<String, Any> = emptyMap()
) {

    val createdTime: Long = System.currentTimeMillis()

    private val variables: ConcurrentHashMap<String, Any> = ConcurrentHashMap(variables)

    val taskGroup: TaskGroup
        get() = TaskGroup.get(viewer.name)

    init {
        updateVariables()
    }

    fun hasValidVariable(key: String): Boolean {
        return !variables.getOrDefault(key, null)?.toString().isNullOrBlank()
    }

    fun hasVariable(key: String): Boolean {
        return variables.containsKey(key)
    }

    fun updateVariables() {
        variables += Invero.API.getDataManager().getGlobalData().source
        variables += viewer.get<Player>().getDataContainer().source
    }

    fun getVariables(ext: Map<String, Any>? = null): Map<String, Any> {
        return variables + (ext ?: emptyMap())
    }

    fun getVariable(key: String): Any? {
        return variables[key]
    }

    fun setVariable(key: String, value: Any) {
        // push to database
        when (key.varType) {
            GLOBAL -> Invero.API.getDataManager().getGlobalData()[key] = value
            PLAYER -> viewer.get<Player>().getDataContainer()[key] = value
            TEMP -> {}
        }
        variables[key] = value
    }

    fun removeVariable(key: String) {
        // push to database
        when (key.varType) {
            GLOBAL -> Invero.API.getDataManager().getGlobalData().source.remove(key)
            PLAYER -> viewer.get<Player>().getDataContainer().source.remove(key)
            TEMP -> {}
        }
        variables.remove(key)
    }

    fun elapsed(): Long {
        return System.currentTimeMillis() - createdTime
    }

    fun parse(input: String, context: Context? = null): String {
        return input.translateFormattedMessage(viewer.get(), context?.variables ?: variables)
    }

    fun parse(input: List<String>, context: Context? = null): List<String> {
        return input.map { parse(it, context) }
    }

    fun pauseAnimatedTitle(last: Long = -1L) {
        setVariable("title_task_running", false)
        if (last > 0) submit(delay = last) { resumeAnimatedTitle() }
    }

    fun resumeAnimatedTitle() = removeVariable("title_task_running")

    companion object {

        private val sessions = ConcurrentHashMap<String, Session>()

        fun getSession(viewer: PlayerViewer): Session? {
            return sessions[viewer.name]
        }

        fun register(viewer: PlayerViewer, menu: Menu, window: BukkitWindow, variables: Map<String, Any>): Session {
            val session = Session(viewer, menu, window, variables)
            sessions[viewer.name] = session
            return session
        }

        fun unregister(session: Session) {
            sessions.remove(session.viewer.name, session)
            session.taskGroup.unregisterAll()
            val viewer = session.viewer
            submitAsync(delay = 40L) { if (viewer.session == null) TaskGroup.get(viewer.name).unregisterAll() }
        }

        val String.varType: VarType
            get() = values().find { startsWith(it.prefix) } ?: TEMP

        enum class VarType {

            TEMP,

            GLOBAL,

            PLAYER;

            val prefix = name.lowercase() + "@"

        }

    }

}