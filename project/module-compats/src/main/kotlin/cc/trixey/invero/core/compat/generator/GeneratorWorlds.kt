package cc.trixey.invero.core.compat.generator

import cc.trixey.invero.common.sourceObject
import cc.trixey.invero.core.compat.DefGeneratorProvider
import cc.trixey.invero.core.generator.BaseGenerator
import org.bukkit.Bukkit

/**
 * Invero
 * cc.trixey.invero.core.compat.generator.GeneratorWorlds
 *
 * @author Arasple
 * @since 2023/2/2 14:34
 */
@DefGeneratorProvider("world")
class GeneratorWorlds : BaseGenerator() {

    override fun generate() {
        generated = Bukkit.getWorlds().map {
            sourceObject {
                put("name", it.name)
                put("uid", it.uid)
                put("environment", it.environment.name)
                put("seed", it.seed)
                put("minHeight", it.minHeight)
                put("maxHeight", it.maxHeight)
                put("allowAnimals", it.allowAnimals)
                put("allowMonsters", it.allowMonsters)
                put("difficulty", it.difficulty)
                put("time", it.time)
            }
        }
    }

}