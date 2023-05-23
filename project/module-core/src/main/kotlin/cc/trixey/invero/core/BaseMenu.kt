@file:OptIn(ExperimentalSerializationApi::class)

package cc.trixey.invero.core

import cc.trixey.invero.common.Invero
import cc.trixey.invero.common.Menu
import cc.trixey.invero.common.MenuActivator
import cc.trixey.invero.common.events.MenuCloseEvent
import cc.trixey.invero.common.events.MenuOpenEvent
import cc.trixey.invero.common.util.prettyPrint
import cc.trixey.invero.core.menu.MenuEvents
import cc.trixey.invero.core.menu.MenuSettings
import cc.trixey.invero.core.menu.MenuTask
import cc.trixey.invero.core.menu.NodeRunnable
import cc.trixey.invero.core.panel.PanelCrafting
import cc.trixey.invero.core.serialize.ListAgentPanelSerializer
import cc.trixey.invero.core.serialize.NodeSerializer
import cc.trixey.invero.core.util.containsAnyPlaceholder
import cc.trixey.invero.core.util.session
import cc.trixey.invero.core.util.translateFormattedMessage
import cc.trixey.invero.core.util.unregisterSession
import cc.trixey.invero.ui.bukkit.InventoryPacket
import cc.trixey.invero.ui.bukkit.InventoryVanilla
import cc.trixey.invero.ui.bukkit.PlayerViewer
import cc.trixey.invero.ui.bukkit.api.dsl.commonWindow
import cc.trixey.invero.ui.bukkit.panel.CraftingPanel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject
import org.bukkit.entity.Player
import taboolib.common.platform.function.submitAsync
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.platform.util.giveItem
import java.util.concurrent.CompletableFuture

/**
 * Invero
 * cc.trixey.invero.core.BaseMenu
 *
 * @author Arasple
 * @since 2023/1/15 17:16
 */
@Serializable
class BaseMenu(
    override var id: String?,
    @SerialName("menu")
    val settings: MenuSettings,
    @JsonNames("bindings", "binding", "activator", "activators")
    val bindings: JsonObject?,
    @JsonNames("event", "listener")
    val events: MenuEvents?,
    @JsonNames("node", "scripts")
    val nodes: Map<String, @Serializable(with = NodeSerializer::class) NodeRunnable>?,
    @JsonNames("task")
    val tasks: Map<String, MenuTask>?,
    @Serializable(with = ListAgentPanelSerializer::class)
    @JsonNames("panel", "pane", "panes")
    val panels: List<AgentPanel>,
) : Menu {

    /**
     * 菜单初始化
     */
    init {
        // 一些情况下的菜单和面板尺寸的自动化实现
        if (settings.rows == null)
            settings.rows = panels
                .maxBy { it.scale.height }
                .scale
                .height
                .coerceIn(1..6)
        panels.forEach {
            if (it.scale.height < 0) it.scale.height = settings.rows ?: 1
        }
        // 如果包含任意一个涉及物品交互的面板，则禁用菜单对玩家背包的覆盖
        if (panels.any { it is PanelCrafting }) {
            settings.setProperty("hidePlayerInventory", false)
        }
    }

    /**
     * 已读取、注册的菜单激活器
     */
    @Transient
    val activators: LinkedHashMap<String, MenuActivator<*>> = LinkedHashMap()

    /**
     * 菜单开启
     *
     * @param viewer 玩家
     * @param vars 会话变量
     */
    override fun open(viewer: PlayerViewer, vars: Map<String, Any>) {
        // 创建 UI Window
        val window = commonWindow(
            viewer,
            virtual = isVirtual(),
            type = settings.containerType,
            hidePlayerInventory = settings.hidePlayerInventory,
            overridePlayerInventory = settings.overridePlayerInventory,
            title = settings.title.default.translateFormattedMessage(viewer.get(), vars),
        ).onClose {
            // restore collection
            val player = viewer.get<Player>()
            it.panels.filterIsInstance<CraftingPanel>().forEach { panel ->
                player.giveItem(panel.freeSlots.mapNotNull { slot -> panel.storage[slot] })
            }
            // close callback (internal)
            close(viewer, closeWindow = false, closeInventory = false)
        }
        // 创建菜单开启事件
        val event = MenuOpenEvent(viewer.get(), this, window, vars).also { it.call() }
        if (event.isCancelled) return
        // 注销原有菜单会话
        if (viewer.session != null) {
            val session = viewer.session
            if (session != null && session.elapsed() < 2_00) return
            else viewer.unregisterSession()
        }
        // 注册菜单会话
        val session = Session.register(viewer, this, window, vars)
        // 开始处理窗口开启

        (events?.preOpen(session) ?: CompletableFuture.completedFuture(true)).thenApply {
            if (!it) return@thenApply
            runCatching {
                // 开启 Window
                // 其本身会检查是否已经打开任何 Window，并自动关闭等效旧菜单的 Window
                window.preOpen {
                    panels.forEach { it.invoke(window, session) }
                    // 部分标题需要菜单语境变量更新的情况
                    if (settings.title.default.containsAnyPlaceholder) {
                        updateTitle(session)
                    }
                }

                window.open()
                // 屏蔽掉频繁的交互
                if (isVirtual())
                    (window.inventory as InventoryPacket).onClick { _, _ -> viewer.canInteract }
                else
                    (window.inventory as InventoryVanilla).onClick { _ -> viewer.canInteract }
                // 应用动态标题属性
                settings.title.submit(session)
                // 应用周期事件
                tasks?.forEach { it.value.submit(session) }
                // 开启后事件动作
                events?.postOpen(session)
            }.onFailure {
                it.prettyPrint()
                Session.unregister(session)
            }
        }
    }

    /**
     * 菜单关闭
     *
     * @param viewer 玩家
     * @param closeWindow 是否关闭窗口
     * @param closeInventory 是否关闭背包
     *
     */
    override fun close(viewer: PlayerViewer, closeWindow: Boolean, closeInventory: Boolean) {
        val session = viewer.session ?: return
        if (session.menu != this) return
        viewer.unregisterSession { if (closeWindow) it.close(true, closeInventory) }
        MenuCloseEvent(viewer.get(), this, session.window).also { it.call() }

        submitAsync { events?.close(session) }
    }

    /**
     * 注册此菜单附带产物
     */
    override fun register() {
        bindings?.forEach { key, value ->
            Invero.API
                .getRegistry()
                .createActivator(this, key, value)
                ?.let { activators[key.lowercase()] = it }
        }
    }

    /**
     * 注销此菜单附带产物
     */
    override fun unregister() {
        activators.forEach { (_, value) -> value.unregister() }
    }

    /**
     * 为一个会话更新容器标题
     */
    fun updateTitle(session: Session) {
        session.window.title = session.parse(settings.title.default)
    }

    /**
     * 本菜单是否为虚拟容器
     */
    override fun isVirtual(): Boolean {
        return settings.virtual && !panels.any { it.requireBukkitWindow() }
    }

    private val PlayerViewer.canInteract: Boolean
        get() = settings.interactBaffle.hasNext(name)


}