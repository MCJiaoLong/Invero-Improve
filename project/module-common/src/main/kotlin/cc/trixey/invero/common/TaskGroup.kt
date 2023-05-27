package cc.trixey.invero.common

import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor
import taboolib.platform.BukkitExecutor
import taboolib.platform.util.bukkitPlugin
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Invero
 * cc.trixey.invero.common.TaskGroup
 *
 * @author Arasple
 * @since 2023/1/16 12:14
 */
class TaskGroup(private val platformTasks: CopyOnWriteArraySet<PlatformExecutor.PlatformTask> = CopyOnWriteArraySet()) {

    fun unregisterAll() {
        platformTasks.removeIf {
            it as BukkitExecutor.BukkitPlatformTask
            if (!it.runnable.isCancelled) {
                it.cancel()
            }
            true
        }
    }

    fun launch(
        now: Boolean = false,
        async: Boolean = false,
        delay: Long = 0,
        period: Long = 0,
        comment: String? = null,
        executor: (task: PlatformExecutor.PlatformTask) -> Unit,
    ) {
        submit(now, async, delay, period, comment, executor).also { this += it }
    }

    fun launchAsync(
        now: Boolean = false,
        delay: Long = 0,
        period: Long = 0,
        comment: String? = null,
        executor: (task: PlatformExecutor.PlatformTask) -> Unit,
    ) = launch(now, true, delay, period, comment, executor)

    operator fun plusAssign(task: PlatformExecutor.PlatformTask) {
        platformTasks += task
    }

    override fun toString(): String {
        return "MenuTasks(platformTasks: ${platformTasks.size})"
    }

    companion object {

        private val taskMgrs = ConcurrentHashMap<String, TaskGroup>()

        fun get(key: String): TaskGroup {
            return taskMgrs.computeIfAbsent(key) { TaskGroup() }
        }

        @Awake(LifeCycle.DISABLE)
        fun unregister() {
            taskMgrs.values.forEach { it.unregisterAll() }

            Bukkit.getScheduler().apply {
                pendingTasks
                    .filter { it.owner == bukkitPlugin && !it.isCancelled }
                    .forEach { it.cancel() }
            }
        }

    }

}