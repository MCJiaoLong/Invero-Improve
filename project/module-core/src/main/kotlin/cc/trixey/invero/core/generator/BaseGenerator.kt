package cc.trixey.invero.core.generator

import cc.trixey.invero.common.ElementGenerator
import cc.trixey.invero.common.Object

/**
 * Invero
 * cc.trixey.invero.core.generator.BaseGenerator
 *
 * @author Arasple
 * @since 2023/2/2 14:39
 */
abstract class BaseGenerator : ElementGenerator {

    override var generated: List<Object>? = null

    override fun generate(context: Any?) = this.generate()

    abstract fun generate()

    override fun filter(block: (Object) -> Boolean): ElementGenerator {
        generated = generated?.filter(block)
        return this
    }

    override fun <R : Comparable<R>> sortBy(block: (Object) -> R): ElementGenerator {
        generated = generated?.sortedBy(block)
        return this
    }

}