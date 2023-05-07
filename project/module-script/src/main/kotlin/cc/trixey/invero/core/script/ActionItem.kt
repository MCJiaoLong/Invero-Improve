package cc.trixey.invero.core.script

import cc.trixey.invero.core.item.TextureHead
import cc.trixey.invero.core.item.TextureMaterial
import cc.trixey.invero.core.item.TextureSource
import cc.trixey.invero.core.script.loader.InveroKetherParser
import org.bukkit.inventory.ItemStack
import taboolib.common.OpenResult
import taboolib.common5.Coerce
import taboolib.common5.cint
import taboolib.module.kether.KetherProperty
import taboolib.module.kether.ScriptProperty
import taboolib.module.kether.combinationParser

/**
 * Invero
 * cc.trixey.invero.core.script.kether.ActionItem
 *
 * @author Arasple
 * @since 2023/2/10 15:34
 */
object ActionItem {

    /*
    item <text from action> amount <int> by <handler> send
     */
    @InveroKetherParser(["item", "itemstack"])
    fun parser() = combinationParser {
        it.group(
            action(),
            command("source", "by", then = text()).option(),
            command("amt", "amount", then = any()).option().defaultsTo(1),
        ).apply(it) { content, handler, amount ->
            now {
                val item = newFrame(content).run<Any>().getNow("<TIMEOUT>")?.toString() ?: error("No item content")
                when (val handle = handler?.lowercase()) {
                    null, "material" -> TextureMaterial(item).lazyTexture
                    "head" -> TextureHead(item).lazyTexture
                    else -> TextureSource(handle, item).lazyTexture
                }
                    ?.apply { setAmount(amount.cint.coerceAtLeast(0)) }
                    ?.clone()
            }
        }
    }

    @KetherProperty(bind = ItemStack::class, shared = true)
    fun propertyItemStack() = object : ScriptProperty<ItemStack>("item.operator.ext") {

        override fun read(instance: ItemStack, key: String): OpenResult {
            return when (key) {
                "amount", "amt", "count" -> OpenResult.successful(instance.amount)
                else -> OpenResult.failed()
            }
        }

        override fun write(instance: ItemStack, key: String, value: Any?): OpenResult {
            return when (key) {
                "amount", "amt", "count" -> {
                    instance.amount = Coerce.toInteger(value)
                    OpenResult.successful()
                }

                else -> OpenResult.failed()
            }
        }

    }

}