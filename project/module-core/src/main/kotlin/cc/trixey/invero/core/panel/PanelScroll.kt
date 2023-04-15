@file:OptIn(ExperimentalSerializationApi::class)

package cc.trixey.invero.core.panel

import cc.trixey.invero.core.AgentPanel
import cc.trixey.invero.core.Layout
import cc.trixey.invero.core.Session
import cc.trixey.invero.core.icon.Icon
import cc.trixey.invero.core.serialize.MappedIconSerializer
import cc.trixey.invero.core.serialize.PosSerializer
import cc.trixey.invero.core.serialize.ScaleSerializer
import cc.trixey.invero.ui.bukkit.PanelContainer
import cc.trixey.invero.ui.bukkit.api.dsl.scroll
import cc.trixey.invero.ui.bukkit.api.dsl.standard
import cc.trixey.invero.ui.bukkit.panel.ScrollStandardPanel
import cc.trixey.invero.ui.common.Pos
import cc.trixey.invero.ui.common.Scale
import cc.trixey.invero.ui.common.panel.PanelWeight
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonNames

/**
 * Invero
 * cc.trixey.invero.core.panel.PanelScroll
 *
 * @author Arasple
 * @since 2023/2/9 22:52
 */
@Serializable
class PanelScroll(
    @Serializable(with = ScaleSerializer::class)
    @SerialName("scale")
    private val _scale: Scale?,
    override val layout: Layout?,
    @Serializable(with = PosSerializer::class)
    override val locate: Pos?,
    val scroll: StructureScroll,
    @JsonNames("icon", "item", "items")
    @Serializable(with = MappedIconSerializer::class)
    override val icons: Map<String, Icon>
) : AgentPanel(), IconContainer {

    init {
        registerIcons()
        scroll.colums.forEachIndexed { columIndex, colum ->
            colum.forEachIndexed { iconIndex, icon ->
                val tag = "colum_${columIndex}_icon_$iconIndex"
                registerIcon(icon, tag)
            }
        }
    }

    @Transient
    override val scale = run {
        require(layout != null) { "PanelScroll must specifiy a layout" }
        _scale ?: layout.getScale()
    }

    @Transient
    val scrollDistribution = run {
        layout!!.findRectangle() ?: error("Can not find valid rectangle within the layout for scroll area")
    }

    override fun invoke(parent: PanelContainer, session: Session): ScrollStandardPanel {
        val (scrollLocate, scrollScale) = scrollDistribution

        // 背景
        parent.standard(scale.pair, parent.locate(), weight = PanelWeight.BACKGROUND) {
            icons.forEach { (_, icon) -> icon.invoke(session, this@PanelScroll, this@standard) }
        }

        // 滚动区域
        return parent.scroll(
            scrollScale.pair,
            scrollLocate.value,
            direction = scroll.direction,
            tail = scroll.tail ?: -1
        ) {
            scroll.colums.forEach { icons ->
                insertColum {
                    if (it <= icons.lastIndex)
                        icons[it].invoke(session, this@PanelScroll, this)
                    else
                        null
                }
            }
            val defShift = session.parse(scroll.defaultIndex?.content ?: "-1").toIntOrNull() ?: -1
            if (defShift > 0) {
                scroll.defaultIndex?.let {
                    shift(defShift, defShift)
                }
            }
        }
    }

}