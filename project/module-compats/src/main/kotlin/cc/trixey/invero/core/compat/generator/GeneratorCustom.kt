package cc.trixey.invero.core.compat.generator

import cc.trixey.invero.common.Object
import cc.trixey.invero.core.compat.DefGeneratorProvider
import cc.trixey.invero.core.generator.BaseGenerator

/**
 * Invero
 * cc.trixey.invero.core.compat.generator.GeneratorCustom
 *
 * @author Arasple
 * @since 2023/1/29 22:30
 */
@DefGeneratorProvider("custom")
class GeneratorCustom : BaseGenerator() {

    override var generated: List<Object>? = listOf()

    override fun generate() {}

}