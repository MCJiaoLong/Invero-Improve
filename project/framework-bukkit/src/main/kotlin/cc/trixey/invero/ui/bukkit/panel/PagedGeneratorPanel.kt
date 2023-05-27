package cc.trixey.invero.ui.bukkit.panel

import cc.trixey.invero.ui.bukkit.BukkitPanel
import cc.trixey.invero.ui.bukkit.PanelContainer
import cc.trixey.invero.ui.bukkit.api.dsl.ruin
import cc.trixey.invero.ui.bukkit.api.dsl.set
import cc.trixey.invero.ui.bukkit.element.Clickable
import cc.trixey.invero.ui.bukkit.element.item.BaseItem
import cc.trixey.invero.ui.bukkit.util.proceed
import cc.trixey.invero.ui.common.Elements
import cc.trixey.invero.ui.common.Pos
import cc.trixey.invero.ui.common.Scale
import cc.trixey.invero.ui.common.event.ClickType
import cc.trixey.invero.ui.common.panel.GeneratorPanel
import cc.trixey.invero.ui.common.panel.PagedPanel
import cc.trixey.invero.ui.common.panel.PanelWeight
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * Invero
 * cc.trixey.invero.ui.bukkit.panel.PagedGeneratorPanel
 *
 * @author Arasple
 * @since 2023/1/11 16:08
 */
class PagedGeneratorPanel<T>(
    parent: PanelContainer,
    weight: PanelWeight,
    scale: Scale,
    locate: Pos
) : BukkitPanel(parent, weight, scale, locate), PagedPanel, GeneratorPanel<T, BaseItem<*>> {

    override var generatorSource: () -> List<T> = { emptyList() }
    override var generatorOutput: (T) -> BaseItem<*>? = { null }
    override var filter: (T) -> Boolean = { true }
    override var comparator: Comparator<T> = compareBy { 0 }

    override val elements: Elements = Elements()

    override var maxPageIndex: Int = 0
    override var pageChangeCallback: PagedPanel.(fromPage: Int, toPage: Int) -> Unit = { _, _ -> }
    override var pageIndex: Int = 0
        set(value) {
            if (value < 0) error("Page index can not be a negative number")
            else if (pageIndex > maxPageIndex) error("Page index is out of bounds $maxPageIndex")
            pageChangeCallback(this, field, value)
            field = value
            render()
        }

    private var lastGenerated: List<T>? = null
    private var lastElements: Array<BaseItem<*>?> = arrayOfNulls(1)

    override fun reset() {
        pageIndex = 0
        removeGeneratedElements()
        lastGenerated = null
        lastElements.forEach { it?.ruin(wipe = false) }
    }

    private fun removeGeneratedElements() {
        elements.value.entries.removeIf {
            val element = it.key as BaseItem<*>
            (element in lastElements).proceed { element.ruin(wipe = false) }
        }
    }

    override fun render() {
        // ruin previously generated elements
        removeGeneratedElements()
        // generator pool
        val pool = elementsPool()
        // re-generate & set max page
        if (lastGenerated == null) {
            lastGenerated = generatorSource().filter(filter).sortedWith(comparator).also {
                maxPageIndex = (it.size - 1) / pool.size
                lastElements = arrayOfNulls<BaseItem<*>?>(it.size)
            }
        }
        // page layouts
        val generated = lastGenerated!!
        val fromIndex = pageIndex * pool.size
        val toIndex = (fromIndex + pool.lastIndex).coerceAtMost(generated.lastIndex)

        if (generated.size > fromIndex) {
            // generate outputs
            for (i in fromIndex..toIndex) {
                val pos = pool[i - fromIndex]
                lastElements[i] = generatorOutput(generated[i]).also {
                    it?.set(pos)
                }
            }
            // clear unused slots
            val last = toIndex - fromIndex
            if (last < pool.lastIndex) {
                (last + 1..pool.lastIndex)
                    .map { pool[it] }
                    .let { col -> wipe(col) }
            }
        }

        // render elements
        super.render()
    }

    override fun handleClick(pos: Pos, clickType: ClickType, e: InventoryClickEvent?): Boolean {
        elements.findElement(pos)?.let {
            if (it is Clickable<*>) {
                it.runClickCallbacks(clickType, e)
                return true
            }
        }
        return false
    }

}