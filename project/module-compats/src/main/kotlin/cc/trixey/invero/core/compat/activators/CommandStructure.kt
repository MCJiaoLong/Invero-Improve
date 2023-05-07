@file:OptIn(ExperimentalSerializationApi::class)

package cc.trixey.invero.core.compat.activators

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * Invero
 * cc.trixey.invero.core.compat.activators.CommandStructure
 *
 * @author Arasple
 * @since 2023/2/25 15:22
 */
@Serializable
class CommandStructure(
    @SerialName("name")
    val rawName: String,
    val aliases: List<String>?,
    val description: String?,
    val usage: String?,
    val permission: String?,
    val permissionMessage: String?,
    @JsonNames("argument", "args")
    val arguments: List<@Serializable(with = CommandArgumentSerializer::class) CommandArgument>?
) {

    @Transient
    val name = rawName.lowercase()

}


@Serializable
class CommandArgument(
    @JsonNames("key", "name", "label")
    val id: String,
    val type: Type?,
    val suggest: List<String>?,
    val restrict: Boolean = false,
    @JsonNames("optional")
    val optional: Boolean = true,
    val default: JsonPrimitive?,
    val incorrectMessage: String?
) {

    enum class Type {

        ANY,

        DECIMAL,

        INTEGER,

        BOOLEAN,

        PLAYER,

        WORLD

    }

}

@Serializer(forClass = CommandArgument::class)
internal object CommandArgumentSerializer : JsonTransformingSerializer<CommandArgument>(serializer()) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        return if (element is JsonPrimitive) {
            buildJsonObject {
                put("id", element.content)
                put("type", "ANY")
            }
        } else {
            element
        }
    }

}