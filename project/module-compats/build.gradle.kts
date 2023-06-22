plugins {
    kotlin("plugin.serialization") version "1.7.21"
}

repositories {
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
}

dependencies {
    compileTabooLib()
    compileCore(11903)
    serialization()
    framework()
    compileLocal(project, "libs")

    compileModule("module-common")
    compileModule("module-core")
    compileOnly("com.github.oraxen:oraxen:-SNAPSHOT")
    compileOnly("ink.ptms.adyeshach:all:2.0.0-snapshot-28")
    compileOnly("ink.ptms:Zaphkiel:2.0.14")
    compileOnly("org.black_ixx:playerpoints:3.1.1")
    compileOnly("com.github.FrancoBM12:API-MagicCosmetics:2.2.2")
}