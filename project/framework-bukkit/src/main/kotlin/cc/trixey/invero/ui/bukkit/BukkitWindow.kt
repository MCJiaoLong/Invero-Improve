package cc.trixey.invero.ui.bukkit

import cc.trixey.invero.ui.bukkit.api.findWindow
import cc.trixey.invero.ui.bukkit.api.isRegistered
import cc.trixey.invero.ui.bukkit.api.registerWindow
import cc.trixey.invero.ui.bukkit.api.unregisterWindow
import cc.trixey.invero.ui.bukkit.nms.handler
import cc.trixey.invero.ui.bukkit.nms.persistContainerId
import cc.trixey.invero.ui.bukkit.nms.updateTitle
import cc.trixey.invero.ui.bukkit.util.synced
import cc.trixey.invero.ui.common.ContainerType
import cc.trixey.invero.ui.common.Scale
import cc.trixey.invero.ui.common.Window
import cc.trixey.invero.ui.common.panel.IOPanel
import cc.trixey.invero.ui.common.util.anyInstancePanel
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit

/**
 * Invero
 * cc.trixey.invero.ui.bukkit.BukkitWindow
 *
 * @author Arasple
 * @since 2023/1/20 12:15
 */
abstract class BukkitWindow(
    override val type: ContainerType,
    title: String = "Invero_Untitled",
    override val viewer: PlayerViewer,
    override val hidePlayerInventory: Boolean,
    override val overridePlayerInventory: Boolean,
    val virtual: Boolean,
) : Window, PanelContainer {

    override var title: String = title
        set(value) {
            field = value
            updateTitle(value)
        }

    final override val panels = arrayListOf<BukkitPanel>()

    val anyIOPanel: Boolean by lazy { anyInstancePanel<IOPanel>() }

    override val scale: Scale by lazy { Scale(9 to 6) }

    private var closeCallback: (BukkitWindow) -> Unit = { _ -> }

    private var openCallback: (BukkitWindow) -> Unit = { _ -> }

    private var preOpenCallback: (BukkitWindow) -> Any = { _ -> true }

    private var preCloseCallback: (BukkitWindow) -> Unit = { _ -> }

    private var preRenderCallback: (BukkitWindow) -> Unit = { _ -> }

    abstract override val inventory: ProxyBukkitInventory

    fun onClose(block: (BukkitWindow) -> Unit): BukkitWindow {
        closeCallback = block
        return this
    }

    fun onOpen(block: (BukkitWindow) -> Unit): BukkitWindow {
        openCallback = block
        return this
    }

    fun preOpen(block: (BukkitWindow) -> Any): BukkitWindow {
        preOpenCallback = block
        return this
    }

    fun preClose(block: (BukkitWindow) -> Unit): BukkitWindow {
        preCloseCallback = block
        return this
    }

    fun preRender(block: (BukkitWindow) -> Unit): BukkitWindow {
        preRenderCallback = block
        return this
    }

    override fun open() {
        val player = viewer.get<Player>()

        // 如果被取消
        if (preOpenCallback(this) == false) return
        // 正在查看一个 Window，则伪关闭
        findWindow(viewer.name)?.unregisterWindow()
        // 注册窗口
        registerWindow()
        // 开启新容器
        // 避免更新标题带来的残影
        val invokable: () -> Unit = {
            preRenderCallback(this)
            render()
            inventory.open()
            openCallback(this)
        }
        submit(delay = 2L) {
            invokable()
        }
    }

    override fun close(doCloseInventory: Boolean, updateInventory: Boolean) {
        require(isRegistered()) { "Can not close an unregistered window" }

        preCloseCallback(this)
        unregisterWindow()

        synced {
            val player = viewer.get<Player>()

            if (doCloseInventory && isViewing()) {
                if (virtual) handler.sendWindowClose(player, persistContainerId)
                else player.closeInventory()
            }
            if (updateInventory) player.updateInventory()

            submit(delay = 2L) {
                if (findWindow(player.name) != null) return@submit

                if (anyIOPanel) {
                    storageMap.remove(player.uniqueId)
                } else {
                    player.restorePlayerInventory()
                }
                player.updateInventory()
            }
        }

        closeCallback(this)
    }

    override fun render() {
        require(panels.all { it.parent == this })

        panels.filterNot { it.skipRender }.sortedByDescending { it.weight }.forEach { it.render() }
    }

    override fun isViewing(): Boolean {
        return inventory.isViewing()
    }

}