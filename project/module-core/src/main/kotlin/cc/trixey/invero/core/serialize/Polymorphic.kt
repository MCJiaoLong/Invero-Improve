@file:OptIn(ExperimentalSerializationApi::class)

package cc.trixey.invero.core.serialize

import cc.trixey.invero.core.AgentPanel
import cc.trixey.invero.core.action.*
import cc.trixey.invero.core.item.Texture
import cc.trixey.invero.core.item.TextureHead
import cc.trixey.invero.core.item.TextureMaterial
import cc.trixey.invero.core.item.TextureSource
import cc.trixey.invero.core.panel.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.*

/**
 * Invero
 * cc.trixey.invero.serialize.Polymorphic
 *
 * @author Arasple
 * @since 2023/1/18 10:47
 */
internal object SelectorAction : JsonContentPolymorphicSerializer<Action>(Action::class) {

    private val serializers = buildMap<DeserializationStrategy<Action>, Set<String>> {
        put(ActionKether.serializer(), setOf("kether"))
        put(ConditionIf.serializer(), setOf("if"))
        put(ConditionIfNot.serializer(), setOf("if not", "if_not"))
        put(ConditionAll.serializer(), setOf("all"))
        put(ConditionAny.serializer(), setOf("any"))
        put(ConditionNone.serializer(), setOf("none"))
        put(ConditionCase.serializer(), setOf("when", "case"))
        put(FunctionalActionCatchers.serializer(), setOf("catchers"))
        put(FunctionalActionCatcher.serializer(), setOf("catch", "afterInput"))
    }

    val structuredKeys = serializers.values.flatten()

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Action> {
        return when (element) {
            is JsonPrimitive -> ActionKether.serializer()
            is JsonArray -> NetesedAction.serializer()
            else -> {
                val keys = element.jsonObject.keys
                serializers
                    .entries
                    .find { it.value.any { s -> s in keys } }
                    ?.key ?: error("Unregonized action [$keys]")
            }
        }
    }

}

internal object SelectorTexture : JsonContentPolymorphicSerializer<Texture>(Texture::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Texture> {
        return if (element is JsonObject) {
            val keys = element.jsonObject.keys
            when {
                "head" in keys -> TextureHead.serializer()
                "source" in keys -> TextureSource.serializer()
                else -> error("Unregonized texture format: $element")
            }
        } else {
            require(element is JsonPrimitive)
            TextureMaterial.serializer()
        }
    }

}

internal object SelectorAgentPanel : JsonContentPolymorphicSerializer<AgentPanel>(AgentPanel::class) {

    override fun selectDeserializer(element: JsonElement): KSerializer<out AgentPanel> {
        val keys = element.jsonObject
        return when {
            // generator -> PanelGenerator... etc
            "pages" in keys -> PanelPaged.serializer()
            "generator" in keys -> PanelGenerator.serializer()
            "scroll" in keys -> PanelScroll.serializer()
            "crafting" in keys -> PanelCrafting.serializer()
            else -> PanelStandard.serializer()
        }
    }

}