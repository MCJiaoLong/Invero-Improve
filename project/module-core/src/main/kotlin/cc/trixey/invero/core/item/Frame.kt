package cc.trixey.invero.core.item

import cc.trixey.invero.core.icon.Slot
import cc.trixey.invero.core.serialize.ListSlotSerializer
import cc.trixey.invero.core.serialize.ListStringSerializer
import cc.trixey.invero.core.util.containsAnyPlaceholder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import taboolib.common5.cshort
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.kether.inferType
import taboolib.module.nms.ItemTag
import taboolib.module.nms.ItemTagData

/**
 * Invero
 * cc.trixey.invero.core.item.WindowFrame
 *
 * @author Arasple
 * @since 2023/1/16 11:51
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
class Frame(
    @JsonNames("last")
    val delay: Long?,
    @Serializable
    @JsonNames("material", "mat")
    val texture: Texture?,
    val name: String?,
    @Serializable(with = ListStringSerializer::class)
    @JsonNames("lores")
    val lore: List<String>?,
    @Serializable @JsonNames("count", "amt")
    val amount: JsonPrimitive?,
    @JsonNames("durability", "dur")
    val damage: JsonPrimitive?,
    @JsonNames("model")
    val customModelData: JsonPrimitive?,
    val color: String?,
    @JsonNames("shiny")
    val glow: JsonPrimitive?,
    @JsonNames("enchantment", "enchant")
    val enchantments: Map<String, Int>?,
    @Serializable(with = ListStringSerializer::class)
    @JsonNames("flag")
    val flags: List<String>?,
    val unbreakable: JsonPrimitive?,
    val nbt: Map<String, JsonPrimitive>?,
    @Serializable(with = ListSlotSerializer::class)
    @JsonNames("slots", "pos", "position", "positions")
    val slot: List<Slot>?,
    val enhancedLore: Boolean?
) {

    init {
        // yaml |- 多行写法
        if (lore != null && lore.size == 1) {
            setProperty("lore", lore.single().split("\n"))
        }
    }

    @Transient
    internal val staticAmount = amount?.intOrNull

    @Transient
    internal val staticDamage = damage?.intOrNull?.cshort

    @Transient
    internal val staticCustomModelData = customModelData?.intOrNull

    @Transient
    internal val staticGlow = glow?.booleanOrNull

    @Transient
    internal val staticUnbreakable = unbreakable?.booleanOrNull

    @Transient
    internal val staticNBT =
        if (nbt == null || nbt.values.any { it.content.containsAnyPlaceholder }) null
        else {
            val tag = ItemTag()
            nbt.forEach { (key, value) -> tag[key] = ItemTagData.toNBT(value.content.inferType()) }
            tag
        }

    fun buildNBT(translator: (String) -> String): ItemTag {
        val tag = ItemTag()
        nbt!!.forEach { (key, value) -> tag[key] = ItemTagData.toNBT(translator(value.content).inferType()) }
        return tag
    }

    fun inherit(frame: Frame) = arrayOf(
        "texture",
        "name",
        "lore",
        "amount",
        "damage",
        "customModelData",
        "color",
        "glow",
        "enchantments",
        "flags",
        "unbreakable",
        "nbt",
        "slot",
        "enhancedLore"
    ).forEach {
        if (getProperty<Any?>(it) == null) {
            val copy = frame.getProperty<Any>(it)
            if (copy is Texture) setProperty(it, copy.clone())
            else setProperty(it, copy)
        }
    }

}