package cc.trixey.invero.core.item

import cc.trixey.invero.core.session.Session
import cc.trixey.invero.core.util.MATERIAL_ID
import cc.trixey.invero.core.util.containsAnyPlaceholder
import kotlinx.serialization.*
import kotlinx.serialization.encoding.*
import org.bukkit.inventory.ItemStack
import taboolib.common.util.Strings
import taboolib.library.xseries.XMaterial
import java.util.concurrent.CompletableFuture
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
    val containsPlaceholder = raw.containsAnyPlaceholder()

    val lazyMaterial by lazy {
        if (containsPlaceholder) null
        else generate(raw) ?: DEFAULT_TEXTURE
    }

    override fun generateItem(session: Session) = CompletableFuture<ItemStack>().apply {
        val texture = if (!containsPlaceholder) lazyMaterial!!
        else generate(session.parse(raw)) ?: DEFAULT_TEXTURE

        complete(texture)
    }

    private fun generate(material: String): ItemStack? {
        @Suppress("DEPRECATION", "Minecraft versions 1.8-1.12")
        MATERIAL_ID
            .matchEntire(material)
            ?.let {
                val id = it.groupValues[1].toIntOrNull() ?: 1
                val data = it.groupValues.getOrNull(2)?.toByteOrNull() ?: 0

                return XMaterial.matchXMaterial(id, data).getOrNull()?.parseItem()
            }

        // 1.13+
        val cleanForm = material
            .uppercase()
            .replace('-', '_')
            .replace(" ", "")

        return XMaterial.matchXMaterial(cleanForm).getOrElse {
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
            return TextureMaterial(decoder.decodeString())
        }

    }

}