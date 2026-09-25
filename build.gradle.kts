plugins {
    java
    id("net.neoforged.moddev") version "1.0.21"
}

version = project.property("mod_version") as String
group = project.property("mod_group") as String

base {
    archivesName.set("mooncake")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

neoForge {
    version = project.property("neo_version") as String

    parchment {
        minecraftVersion = project.property("minecraft_version") as String
        mappingsVersion = "2024.11.17"
    }

    runs {
        register("client") {
            client()
        }
        register("server") {
            server()
        }
    }

    mods {
        register("mooncake") {
            sourceSet(sourceSets.main.get())
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.blamejared.com")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.createmod.net")
    maven("https://maven.ithundxr.dev/snapshots")
}

configurations {
    create("localRuntime")
    named("runtimeClasspath") {
        extendsFrom(configurations["localRuntime"])
    }
}

dependencies {
    // Dev client only / optional compat
    compileOnly("mezz.jei:jei-${property("minecraft_version")}-neoforge:${property("jei_version")}")
    add("localRuntime", "mezz.jei:jei-${property("minecraft_version")}-neoforge:${property("jei_version")}")
    compileOnly("maven.modrinth:jade:${property("jade_version")}")
    add("localRuntime", "maven.modrinth:jade:${property("jade_version")}")

    // Create — optional compat (compile for mixins; runtime in runClient)
    compileOnly("com.simibubi.create:create-${property("minecraft_version")}:${property("create_version")}:slim") {
        isTransitive = false
    }
    compileOnly("net.createmod.ponder:ponder-neoforge:${property("ponder_version")}+mc${property("minecraft_version")}")
    compileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${property("minecraft_version")}:${property("flywheel_version")}")
    compileOnly("com.tterrag.registrate:Registrate:${property("registrate_version")}")
    add("localRuntime", "com.simibubi.create:create-${property("minecraft_version")}:${property("create_version")}:slim") {
        isTransitive = false
    }
    add("localRuntime", "net.createmod.ponder:ponder-neoforge:${property("ponder_version")}+mc${property("minecraft_version")}")
    add("localRuntime", "dev.engine-room.flywheel:flywheel-neoforge-${property("minecraft_version")}:${property("flywheel_version")}")
    add("localRuntime", "com.tterrag.registrate:Registrate:${property("registrate_version")}")

    // Farmer's Delight — optional cutting-board compat (runtime in runClient)
    add("localRuntime", "maven.modrinth:farmers-delight:${property("farmers_delight_version")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("mod_version" to project.version)
    inputs.properties(props)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(props)
    }
}
