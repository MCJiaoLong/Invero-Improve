package cc.trixey.invero.core.api

import cc.trixey.invero.common.*
import cc.trixey.invero.common.api.InveroRegistry
import cc.trixey.invero.core.BaseMenu
import kotlinx.serialization.json.JsonElement
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common5.Baffle
import taboolib.platform.util.bukkitPlugin
import java.util.concurrent.TimeUnit

/**
 * Invero
 * cc.trixey.invero.core.api.DefaultInveroRegistry
 *
 * @author Arasple
 * @since 2023/2/25 14:20
 */
class DefaultInveroRegistry : InveroRegistry {

    private val menuActivatorRegistry = LinkedHashMap<String, MenuActivator<*>>()
    private val activatorBaffle by lazy {
        Baffle.of(1_000, TimeUnit.MILLISECONDS)
    }

    private val itemSourceProvider = LinkedHashMap<String, ItemSourceProvider>()
    private val elementGenerators = LinkedHashMap<String, ElementGenerator>()

    override fun registerActivator(name: String, activator: MenuActivator<*>) {
        menuActivatorRegistry[name.lowercase()] = activator
    }

    override fun createActivator(menu: Menu, name: String, jsonElement: JsonElement): MenuActivator<*>? {
        return (menuActivatorRegistry[name.lowercase()]?.deserialize(jsonElement) as? MenuActivator<*>)?.also {
            it.setActivatorMenu(menu)
        }
    }

    override fun callActivator(player: Player, name: String, vararg params: Any): Boolean {
        if (!activatorBaffle.hasNext(player.name)) return false

        return Invero.API.getMenuManager().getMenus().any {
            it as BaseMenu
            it.activators[name.lowercase()]?.call(player, *params) == true
        }
    }

    override fun registerElementGenerator(namespace: String, id: String, provider: ElementGenerator) {
        val identifier = if (namespace == bukkitPlugin.name) id else "$namespace:$id"
        elementGenerators[identifier] = provider
    }

    override fun registerItemSourceProvider(name: String, provider: ItemSourceProvider) {
        itemSourceProvider[name.uppercase()] = provider
    }

    override fun getItemSourceProvider(name: String): ItemSourceProvider? {
        return itemSourceProvider[name.uppercase()]
    }

    override fun createElementGenerator(identifier: String): ElementGenerator? {
        return elementGenerators.entries.find {
            it.key.equals(identifier, true) || identifier.removeSuffix("s").equals(it.key.removeSuffix("s"), true)
        }?.value?.javaClass?.getConstructor()?.newInstance()
    }


    override fun registerAction(name: String, action: MenuAction) {

    }

    companion object {

        @Awake(LifeCycle.INIT)
        fun init() {
            PlatformFactory.registerAPI<InveroRegistry>(DefaultInveroRegistry())
        }

    }

}