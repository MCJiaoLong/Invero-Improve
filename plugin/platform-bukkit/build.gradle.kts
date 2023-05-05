import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version shadowJarVersion
}

dependencies {
    compileTabooLib()
    compileCore(11903)

    rootProject
        .childProjects["project"]!!
        .childProjects
        .values
        .forEach { implementation(it) }
}

tasks {
    withType<ShadowJar> {
        // options
        archiveAppendix.set("")
        archiveClassifier.set("")
        archiveVersion.set(rootVersion)
        archiveBaseName.set(rootName)
        // exclude
        exclude("META-INF/**")
        exclude("com/**", "org/**")
        // adventure
        relocate("net.kyori", "$rootGroup.common.adventure")
        // taboolib
//        relocate("com.electronwill.nightconfig", "com.electronwill.nightconfig_3_6_6")
        relocate("taboolib", "$rootGroup.taboolib")
        relocate("tb", "$rootGroup.taboolib")
        relocate("org.tabooproject", "$rootGroup.taboolib.library")
        // kotlin
        relocate("kotlin.", "kotlin1820.") { exclude("kotlin.Metadata") }
        relocate("kotlinx.serialization", "kotlinx150.serialization")
    }
    build {
        dependsOn(shadowJar)
    }
}