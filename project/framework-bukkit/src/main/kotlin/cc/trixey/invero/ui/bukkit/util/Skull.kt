package cc.trixey.invero.ui.bukkit.util

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submitAsync
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.modifyMeta
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Invero
 * cc.trixey.invero.ui.bukkit.util.Skull
 *
 * @author Arasple
 * @since 2023/1/30 17:21
 */
private val mojangAPI = arrayOf(
    "https://api.mojang.com/users/profiles/minecraft/",
    "https://sessionserver.mojang.com/session/minecraft/profile/"
)
private val defaultHead = XMaterial.PLAYER_HEAD.parseItem()!!
private val cachedSkulls = ConcurrentHashMap<String, ItemStack>()

fun requestHead(identifier: String, response: (ItemStack) -> Unit) {
    if (identifier.length > 30)
        requestCustomTextureHead(identifier).also(response)
    else
        requestPlayerHead(identifier, response)
}

private fun requestCustomTextureHead(texture: String) =
    cachedSkulls
        .computeIfAbsent(texture) {
            defaultHead.clone().modifyHeadTexture(texture)
        }
        .clone()

private fun requestPlayerHead(name: String, response: (ItemStack) -> Unit) {
    if (name in cachedSkulls.keys) {
        cachedSkulls[name]?.also(response)
    } else {
        @Suppress("DEPRECATION")
        val player = Bukkit.getPlayerExact(name)
        val playerTexture = player?.getPlayerTexture()
        if (player != null && playerTexture != null) {
            requestCustomTextureHead(playerTexture).also(response)
        } else {
            response(defaultHead.modifyMeta<ItemMeta> { setDisplayName("§8...") })

            submitAsync {
                val profile = JsonParser().parse(fromURL("${mojangAPI[0]}$name")) as? JsonObject
                if (profile == null) {
                    console().sendMessage("§c[I] §7MOJANGAPI Can not found offline player [$name]")
                } else {
                    val uuid = profile["id"].asString
                    (JsonParser().parse(fromURL("${mojangAPI[1]}$uuid")) as JsonObject)
                        .getAsJsonArray("properties")
                        .any {
                            if ("textures" == it.asJsonObject["name"].asString) {
                                requestCustomTextureHead(it.asJsonObject["value"].asString).also { head ->
                                    response(head)
                                    cachedSkulls[name] = head
                                }
                                true
                            } else false
                        }
                }
            }
        }
    }
}

fun ItemStack.searchHeadTexture(): String? {
    val meta = itemMeta as SkullMeta? ?: return null

    return meta
        .getProperty<GameProfile>("profile")
        ?.properties
        ?.values()
        ?.find {
            it.name == "textures"
        }
        ?.value
}

fun OfflinePlayer.getPlayerTexture(): String? {
    return invokeMethod<GameProfile>("getProfile")
        ?.properties
        ?.get("textures")
        ?.find { it.value != null }
        ?.value
}

fun ItemStack.modifyHeadTexture(input: String): ItemStack {
    val profile = GameProfile(UUID.randomUUID(), null)
    val texture =
        if (input.length in 60..100)
            Base64
                .getEncoder()
                .encodeToString("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/$input\"}}}".toByteArray())
        else input
    profile
        .properties
        .put("textures", Property("textures", texture, "Invero_TexturedSkull"))

    return modifyMeta<SkullMeta> { setProperty("profile", profile) }
}