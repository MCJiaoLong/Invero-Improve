import java.text.SimpleDateFormat

const val rootName = "Invero"
const val rootGroup = "cc.trixey.invero"
const val rootVersion = "1.0.7"

const val kotlinVersion = "1.8.10"
const val shadowJarVersion = "8.1.0"
const val taboolibPluginVersion = "1.56"

val taboolibVersion = taboolibLatestVersion.also { println("Using taboolib-version = $it") }
const val repoTabooProject = "http://ptms.ink:8081/repository/releases"

val isoInstantFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

val usedTaboolibModules = setOf(
    "common",
    "common-5",
    "platform-bukkit",
    "module-nms",
    "module-nms-util",
    "module-kether",
    "module-configuration",
    "module-lang",
    "module-chat",
    "module-database",
    "module-metrics",
    "expansion-javascript",
    "expansion-player-database",
)

val usedAdventureModules = setOf(
    "net.kyori:adventure-api:4.12.0",
    "net.kyori:adventure-platform-api:4.2.0",
    "net.kyori:adventure-platform-bukkit:4.2.0",
    "net.kyori:adventure-platform-facet:4.2.0",
    "net.kyori:adventure-text-minimessage:4.12.0",
    "net.kyori:adventure-text-serializer-gson:4.2.0",
    "net.kyori:adventure-text-serializer-bungeecord:4.2.0",
    "net.kyori:adventure-text-serializer-legacy:4.2.0"
)