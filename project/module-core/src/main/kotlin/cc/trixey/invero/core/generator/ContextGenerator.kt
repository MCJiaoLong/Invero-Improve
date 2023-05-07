package cc.trixey.invero.core.generator

import cc.trixey.invero.common.ElementGenerator
import cc.trixey.invero.common.Object
import cc.trixey.invero.core.Context

/**
 * Invero
 * cc.trixey.invero.core.generator.ContextGenerator
 *
 * @author Arasple
 * @since 2023/2/23 9:27
 */
abstract class ContextGenerator : ElementGenerator {

    override var generated: List<Object>? = null

    override fun generate(context: Any?) = this.generate(context as Context)

    abstract fun generate(context: Context)

    override fun filter(block: (Object) -> Boolean): ElementGenerator {
        generated = generated?.filter(block)
        return this
    }

    override fun <R : Comparable<R>> sortBy(block: (Object) -> R): ElementGenerator {
        generated = generated?.sortedBy(block)
        return this
    }

}