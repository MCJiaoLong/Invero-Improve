package cc.trixey.invero.core.compat.generator

import cc.trixey.invero.common.sourceObject
import cc.trixey.invero.core.Context
import cc.trixey.invero.core.compat.DefGeneratorProvider
import cc.trixey.invero.core.generator.ContextGenerator
import org.bukkit.entity.Player

/**
 * Invero
 * cc.trixey.invero.core.compat.generator.GeneratorEntities
 *
 * @author Arasple
 * @since 2023/2/27 7:55
 */

@DefGeneratorProvider("entity_world")
internal class GeneratorEntitiesWorld : ContextGenerator() {

    override fun generate(context: Context) {
        val player = context.viewer.get<Player>()
        generated = player
            .world
            .entities
            .map {
                sourceObject {
                    put("name", it.name)
                    put("type", it.type.name)
                }
            }
    }

}

@DefGeneratorProvider("entity_nearby")
internal class GeneratorEntitiesNearby : ContextGenerator() {

    override fun generate(context: Context) {
        val player = context.viewer.get<Player>()
        generated = player
            .getNearbyEntities(10.0, 10.0, 10.0)
            .map {
                sourceObject {
                    put("name", it.name)
                    put("type", it.type.name)
                }
            }
    }

}