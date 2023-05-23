package cc.trixey.invero.core.command.sub

import cc.trixey.invero.common.util.*
import cc.trixey.invero.common.util.PasteResult.Status.ERROR
import cc.trixey.invero.common.util.PasteResult.Status.SUCCESS
import cc.trixey.invero.core.serialize.ItemStackJsonSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.common.platform.function.submitAsync
import taboolib.platform.util.isAir
import taboolib.platform.util.sendLang
import taboolib.platform.util.serializeToByteArray
import taboolib.type.BukkitEquipment
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Invero
 * cc.trixey.invero.core.command.sub.CommandItem
 *
 * @author Arasple
 * @since 2023/2/13 18:45
 */
@CommandHeader(name = "itemSerializer", permission = "invero.command.item")
object CommandItem {

    @CommandBody
    val encode = subCommand {
        dynamic("slot", optional = true) {
            suggest { BukkitEquipment.values().map { it.nms } }

            execute<Player> { player, ctx, _ ->
                val equipment = BukkitEquipment.fromString(ctx["slot"])
                BukkitEquipment
                    .getItems(player)[equipment]
                    .postItemSerialization(player)
            }
        }
        execute<Player> { player, _, _ ->
            BukkitEquipment
                .getItems(player)[BukkitEquipment.HAND]
                .postItemSerialization(player)
        }
    }

    @CommandBody
    val encodePrint = subCommand {
        dynamic("slot", optional = true) {
            suggest { BukkitEquipment.values().map { it.nms } }

            execute<Player> { player, ctx, _ ->
                val equipment = BukkitEquipment.fromString(ctx["slot"])
                BukkitEquipment
                    .getItems(player)[equipment]
                    .postItemSerialization(player, true)
            }
        }
        execute<Player> { player, _, _ ->
            BukkitEquipment
                .getItems(player)[BukkitEquipment.HAND]
                .postItemSerialization(player, true)
        }
    }

    private fun ItemStack?.postItemSerialization(player: Player, print: Boolean = false) {
        if (isAir) {
            player.sendLang("item-air")
            return
        }
        this!!

        submitAsync {
            player.sendLang("paste-init")

            val serialized = Json.encodeToJsonElement(ItemStackJsonSerializer, this@postItemSerialization).jsonObject.reduceEmpty()
            val view = createContent("Structure View", prettyJson.encodeToString(serialized), "JSON")
            val valueBase64 = Base64.getEncoder().encodeToString(serializeToByteArray())
            val valueJson = standardJson.encodeToString(serialized)
            val base64 = createContent("Format Base64", valueBase64)
            val json = createContent("Format Json", valueJson, "JSON")

            if (print) {
                println("Base64: ")
                println(valueBase64)
                println("Json: ")
                println(valueJson)
            }

            paste(
                "Invero Item Serialization",
                "item serialized to json & base64 format",
                48,
                TimeUnit.HOURS,
                view,
                base64,
                json
            ).apply {
                when (status) {
                    SUCCESS -> player.sendLang("paste-success", anonymousLink)
                    ERROR -> player.sendLang("paste-failed")
                }
            }
        }
    }

}