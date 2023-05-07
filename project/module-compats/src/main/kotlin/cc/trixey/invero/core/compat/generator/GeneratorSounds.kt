package cc.trixey.invero.core.compat.generator

import cc.trixey.invero.common.sourceObject
import cc.trixey.invero.core.compat.DefGeneratorProvider
import cc.trixey.invero.core.generator.BaseGenerator
import org.bukkit.Sound

/**
 * Invero
 * cc.trixey.invero.core.compat.generator.GeneratorSounds
 *
 * @author Arasple
 * @since 2023/2/2 14:34
 */
@DefGeneratorProvider("sound")
class GeneratorSounds : BaseGenerator() {

    override fun generate() {
        generated = Sound.values().map {
            sourceObject {
                put("name", it.name)
                put("ordinal", it.ordinal)
            }
        }
    }

}