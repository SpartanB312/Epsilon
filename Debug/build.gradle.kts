import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.kotlin.daemon.common.usedMemory
import org.spongepowered.asm.gradle.plugins.MixinExtension

val modGroup: String by extra
val modVersion: String by extra

group = modGroup
version = modVersion


buildscript {
    repositories {
        jcenter()
        maven("https://files.minecraftforge.net/maven")
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }

    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:4.+")
        classpath("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    }
}

plugins {
    java
    kotlin("jvm") version "1.6.0"
}

apply {
    plugin("net.minecraftforge.gradle")
    plugin("org.spongepowered.mixin")
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://impactdevelopment.github.io/maven/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://jitpack.io")
}

val library: Configuration by configurations.creating {
}


dependencies {
    val kotlinVersion: String by project
    val kotlinxCoroutineVersion: String by project

    fun minecraft(dependencyNotation: Any): Dependency? =
        "minecraft"(dependencyNotation)

    fun jarOnly(dependencyNotation: Any) {
        library(dependencyNotation)
    }

    fun ModuleDependency.exclude(moduleName: String) =
        exclude(mapOf("module" to moduleName))

    library(kotlin("stdlib", kotlinVersion))
    library(kotlin("reflect", kotlinVersion))
    library(kotlin("stdlib-jdk8", kotlinVersion))
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")

    minecraft("net.minecraftforge:forge:1.12.2-14.23.5.2855")

    library("org.spongepowered:mixin:0.8-SNAPSHOT") {
        exclude("commons-io")
        exclude("gson")
        exclude("guava")
        exclude("launchwrapper")
        exclude("log4j-core")
    }

    library("org.reflections:reflections:0.9.12") {
        exclude("gson")
        exclude("guava")
    }

    library("club.minnced:java-discord-rpc:v2.0.2") {
        exclude("jna")
    }

    library("org.joml:joml:1.10.1")

    library(fileTree("lib"))

    library("com.formdev:flatlaf:1.1.2")
    library("com.formdev:flatlaf-intellij-themes:1.1.2")


    library("com.github.cabaletta:baritone:1.2.14")
    jarOnly("cabaletta:baritone-api:1.2")

    annotationProcessor("org.spongepowered:mixin:0.8.2:processor") {
        exclude("gson")
    }


    implementation(project(":Client"))

    implementation(library)
}

configure<MixinExtension> {
    defaultObfuscationEnv = "searge"
    add(sourceSets["main"], "mixins.epsilon.refmap.json")
}

configure<UserDevExtension> {
    mappings(
        mapOf(
            "channel" to "stable",
            "version" to "39-1.12"
        )
    )

    runs {
        create("client") {
            workingDirectory = project.file("run").path

            properties(
                mapOf(
                    "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP",
                    "forge.logging.console.level" to "info",
                    "fml.coreMods.load" to "com.client.epsilon.launch.FMLCoreMod",
                    "mixin.env.disableRefMap" to "true"
                )
            )
        }
    }
}


tasks {
    compileJava {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xinline-classes")
        }
    }

    jar {
        enabled = true
        manifest {
            attributes(
                "FMLCorePluginContainsFMLMod" to "true",
                "FMLCorePlugin" to "club.eridani.epsilon.debug.FMLCoreMod",
                "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
                "TweakOrder" to 0,
                "ForceLoadAsMod" to "true"
            )
        }
        isZip64 = true
        from(
            library.map {
                if (it.isDirectory) it
                else zipTree(it)
            }

        )
        exclude(
            "META-INF/versions/**",
            "**/*.RSA",
            "**/*.SF",
            "**/module-info.class",
            "**/LICENSE",
            "**/*.txt"
        )
    }

}