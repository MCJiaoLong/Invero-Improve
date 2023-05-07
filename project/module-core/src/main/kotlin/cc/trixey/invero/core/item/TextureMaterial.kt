package cc.trixey.invero.core.item

import cc.trixey.invero.core.Context
import cc.trixey.invero.core.util.MATERIAL_ID
import kotlinx.serialization.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.common.util.Strings
import taboolib.common5.cshort
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.library.xseries.XMaterial
import taboolib.module.nms.MinecraftVersion
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

/**
 * Invero
 * cc.trixey.invero.core.item.TextureMaterial
 *
 * @author Arasple
 * @since 2023/1/16 10:32
 */
@Serializable
class TextureMaterial(override val raw: String) : Texture() {

    @Transient
    override val lazyTexture = run {
        if (containsPlaceholder) null
        else generate(raw) ?: DEFAULT_TEXTURE
    }

    override fun generateItem(context: Context, block: (ItemStack) -> Unit) {
        block(lazyTexture ?: generate(context.parse(raw)) ?: DEFAULT_TEXTURE)
    }

    private fun generate(material: String): ItemStack? {
        @Suppress("DEPRECATION", "Minecraft versions 1.8-1.12")
        MATERIAL_ID
            .matchEntire(material)
            ?.let {
                val id = it.groupValues[1].toIntOrNull() ?: 1
                val data = it.groupValues.getOrNull(2)?.toByteOrNull() ?: 0

                return if (MinecraftVersion.majorLegacy < 11300)
                    Material::class.java.invokeMethod<Material>(
                        "getMaterial",
                        id,
                        isStatic = true,
                    )?.let { mat -> ItemStack(mat, 1, data.cshort) }
                else XMaterial.matchXMaterial(id, data).getOrNull()?.parseItem()
            }

        // 1.13+
        val cleanForm = material
            .uppercase()
            .replace(" +".toRegex(), "_")
            .replace('-', '_')

        return Material.getMaterial(cleanForm)?.let { ItemStack(it) } ?: XMaterial.matchXMaterial(cleanForm).getOrElse {
            XMaterial.values().maxByOrNull { Strings.similarDegree(it.name, cleanForm) }
        }?.parseItem()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Serializer(forClass = TextureMaterial::class)
    companion object : KSerializer<TextureMaterial> {

        override fun serialize(encoder: Encoder, value: TextureMaterial) {
            encoder.encodeString(value.raw)
        }

        override fun deserialize(decoder: Decoder): TextureMaterial {
            decoder as JsonDecoder
            return TextureMaterial(decoder.decodeJsonElement().jsonPrimitive.content)
        }

    }

    override fun clone() = TextureMaterial(raw)

}