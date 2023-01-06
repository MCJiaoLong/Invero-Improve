package cc.trixey.invero.test.unit

import cc.trixey.invero.bukkit.api.dsl.*
import cc.trixey.invero.bukkit.util.randomMaterial
import cc.trixey.invero.common.Pos
import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit

/**
 * @author Arasple
 * @since 2023/1/6 14:45
 */
fun showFreeformStandard(player: Player) = bukkitChestWindow(6, "FreeformPanel Standard") {

    freeformPanel(9 to 6) {

        /*
        占据 0,0 到 5,5 的所有区域
         */
        for (x in 0..5)
            for (y in 0..5)
                buildItem(randomMaterial()) {
                    onClick { player.sendMessage("Build on $x to $y") }
                }.add(Pos(x to y))

        /*
        从 6，0 开始沿 4 格长的斜线 来回跑的一个苹果
         */
        val startX = 6
        val startY = 0
        var appleCount = 1
        var count = 0
        var reversing = false
        val runningApple = item(startX to startY, Material.APPLE) {
            modify { name = "Running_Apple" }

            onClick {
                modify { amount = ++appleCount }
                push()
            }
        }

        submit(now = false, async = true, 20L, 20L) {
            if (!hasViewer()) cancel().also {
                println("Cancelled")
            }

            runningApple.set(Pos((startX + count) to (startY + count)))
            runningApple.push()

            if (count >= 4 && !reversing) reversing = true
            if (reversing && count <= 0) reversing = false

            if (reversing) count--
            else count++
        }
    }

    nav(3 to 3, 3 to 6) {
        item(Material.GRAY_STAINED_GLASS_PANE) {
            modify { name = "↖" }
            onClick { firstFreeform().upLeft() }
        }
        item(Material.BLUE_STAINED_GLASS_PANE) {
            modify { name = "↑" }
            onClick { firstFreeform().up() }
        }
        item(Material.GRAY_STAINED_GLASS_PANE) {
            modify { name = "↗" }
            onClick { firstFreeform().upRight() }
        }
        item(Material.RED_STAINED_GLASS_PANE) {
            modify { name = "←" }
            onClick { firstFreeform().left() }
        }
        item(Material.BLACK_STAINED_GLASS_PANE) {
            modify { name = "RESET" }
            onClick { firstFreeform().reset() }
        }
        item(Material.GREEN_STAINED_GLASS_PANE) {
            modify { name = "→" }
            onClick { firstFreeform().right() }
        }
        item(Material.GRAY_STAINED_GLASS_PANE) {
            modify { name = "↙" }
            onClick { firstFreeform().downLeft() }
        }
        item(Material.ORANGE_STAINED_GLASS_PANE) {
            modify { name = "↓" }
            onClick { firstFreeform().down() }
        }
        item(Material.ORANGE_STAINED_GLASS_PANE) {
            modify { name = "↘" }
            onClick { firstFreeform().downRight() }
        }
    }

    open(player)
}