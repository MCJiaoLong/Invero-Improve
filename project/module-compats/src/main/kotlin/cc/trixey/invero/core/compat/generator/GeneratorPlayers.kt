package cc.trixey.invero.core.compat.generator

import cc.trixey.invero.common.sourceObject
import cc.trixey.invero.core.compat.DefGeneratorProvider
import cc.trixey.invero.core.generator.BaseGenerator
import taboolib.platform.util.onlinePlayers

/**
 * Invero
 * cc.trixey.invero.core.compat.generator.GeneratorPlayers
 *
 * @author Arasple
 * @since 2023/1/29 22:03
 */
@DefGeneratorProvider("player")
class GeneratorPlayers : BaseGenerator() {

    override fun generate() {
        generated = onlinePlayers.map {
            sourceObject {
                put("instance", it)
                put("name", it.name)
                put("displayName", it.displayName)
                put("isSneaking", it.isSneaking)
                put("isSprinting", it.isSprinting)
                put("x", it.location.x)
                put("y", it.location.y)
                put("z", it.location.z)
                put("yaw", it.location.yaw)
                put("pitch", it.location.pitch)
                put("address", it.address?.hostString)
            }
        }
    }

}