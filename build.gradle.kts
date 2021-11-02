import java.util.Properties

plugins {
    kotlin("jvm") version "1.5.31"
    id("fabric-loom") version "0.10-SNAPSHOT"
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()
}

val mod_version: String by project
val mod_id: String by project
val maven_group: String by project
val minecraft_version: String by project
val loader_version: String by project
val fabric_version: String by project
val fabric_kotlin_version: String by project
val yarn_mappings: String by project
val archives_base_name: String by project
val ffl_version: String by project
version = mod_version
project.group = maven_group

base {
	archivesBaseName = archives_base_name
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://maven.cafeteria.dev")
        content {
            includeGroup("net.adriantodt.fabricmc")
        }
    }
    maven {
        name = "Ladysnake Mods"
        url = uri("https://ladysnake.jfrog.io/artifactory/mods")
        content {
            includeGroup("io.github.ladysnake")
            includeGroupByRegex("io\\.github\\.onyxstudios.*")
        }
    }
}

dependencies {
	minecraft(group = "com.mojang", name = "minecraft", version = minecraft_version)
    mappings(group = "net.fabricmc", name = "yarn", version = yarn_mappings, classifier = "v2")
    modImplementation(group = "net.fabricmc", name="fabric-loader", version = loader_version)
    modImplementation(group = "net.fabricmc.fabric-api", name="fabric-api", version = fabric_version)
    modImplementation(group = "net.fabricmc", name="fabric-language-kotlin", version = fabric_kotlin_version)

    modImplementation(group = "net.adriantodt.fabricmc", name="fallflyinglib", version = ffl_version)
    include(group = "net.adriantodt.fabricmc", name="fallflyinglib", version = ffl_version)
}

tasks.getByName<ProcessResources>("processResources") {
    filesMatching("fabric.mod.json") {
        expand(
            mutableMapOf(
                "modid" to mod_id,
                "version" to mod_version,
                "kotlinVersion" to fabric_kotlin_version,
                "fabricApiVersion" to fabric_version
            )
        )
    }
}