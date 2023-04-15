package cc.trixey.invero.core.panel

import cc.trixey.invero.core.icon.Icon
import cc.trixey.invero.core.serialize.ListIconSerializer
import cc.trixey.invero.ui.common.scroll.ScrollDirection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonPrimitive

/**
 * Invero
 * cc.trixey.invero.core.panel.StructureScroll
 *
 * @author Arasple
 * @since 2023/2/10 13:19
 */
@Serializable
class StructureScroll(
    val tail: Int?,
    @SerialName("direction")
    val _direction: String?,
    @SerialName("default-index")
    val defaultIndex: JsonPrimitive?,
    val colums: List<@Serializable(with = ListIconSerializer::class) List<Icon>>
) {

    @Transient
    val direction: ScrollDirection =
        if (_direction.equals("vertical", true)) ScrollDirection.VERTICAL else ScrollDirection.HORIZONTAL

}