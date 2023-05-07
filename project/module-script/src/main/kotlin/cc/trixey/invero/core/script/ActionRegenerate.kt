package cc.trixey.invero.core.script

import cc.trixey.invero.common.Object
import cc.trixey.invero.core.script.loader.InveroKetherParser
import cc.trixey.invero.core.util.KetherHandler
import cc.trixey.invero.ui.common.panel.GeneratorPanel
import cc.trixey.invero.ui.common.panel.PagedPanel
import org.bukkit.entity.Player
import taboolib.common5.Baffle
import taboolib.common5.cbool
import taboolib.module.kether.combinationParser
import java.util.concurrent.TimeUnit

/**
 * Invero
 * cc.trixey.invero.expansion.script.menu.ActionRegenerate
 *
 * @author Arasple
 * @since 2023/1/19 20:33
 */
object ActionRegenerate {

    private val regeneratorBaffle by lazy {
        Baffle.of(500, TimeUnit.MILLISECONDS)
    }

    // regenerate filter <filter> sort <sortby>
    @InveroKetherParser(["regenerate"])
    fun parserGenerator() = combinationParser {
        it.group(
            command("filter", then = text()).option(),
            command("sort", then = text()).option()
        ).apply(it) { filter, sort ->
            now {
                val session = session() ?: return@now false
                val viewer = session.viewer.get<Player>()
                if (!regeneratorBaffle.hasNext(viewer.name)) return@now false
                val panel = findNearstPanel<GeneratorPanel<Object, *>>() ?: return@now false
                val s = filter ?: contextVar<String>("@raw_filter")

                if (s != null) {
                    panel.filterBy { obj ->
                        KetherHandler
                            .invoke(s, viewer, session.getVariables(ext = obj.variables))
                            .getNow(true).cbool
                    }
                }
                if (sort != null) panel.sortWith { o1, o2 -> o1[sort].toString().compareTo(o2[sort].toString()) }
                if (panel is PagedPanel) panel.pageIndex = 0

                panel.reset()
                panel.render()

                return@now true
            }
        }
    }

}