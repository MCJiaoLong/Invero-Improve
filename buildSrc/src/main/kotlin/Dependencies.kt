import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.PluginAware
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*

val rootName = "Invero"
val rootGroup = "cc.trixey.invero"
val rootVersion = "1.0.0-dev-7"

val kotlinVersion = "1.8.0"
val shadowJarVersion = "7.1.2"
val taboolibVersion = "6.0.10-80"
val taboolibPluginVersion = "1.56"

val repoTabooProject = "https://repo.tabooproject.org/repository/releases"

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
    "expansion-javascript",
    "expansion-player-database",
)

fun PluginAware.applyPlugins() {
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "org.jetbrains.kotlin.jvm")
}

fun Project.buildDirClean() {
    @Suppress("DEPRECATION")
    gradle.buildFinished { buildDir.deleteRecursively() }
}

fun Project.initSubProject(publish: Project.() -> Unit) {
    group = rootGroup
    version = rootVersion

    tasks.withType<JavaCompile> { options.encoding = "UTF-8" }
    @Suppress("DEPRECATION")
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    if (parent?.name != "plugin") {
        buildDirClean()
    }
    if (parent != rootProject && name.startsWith("framework")) {
        publish()
    }
}

fun RepositoryHandler.projectRepositories() {
    maven(repoTabooProject)
    mavenCentral()
}