import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("application")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("shadow")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "cli.CLI"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

application {
    mainClassName = "cli.CLIKt"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(project(":core"))
    implementation("com.github.ajalt.clikt:clikt:3.1.0")

    testImplementation(project(path = ":core", configuration = "testArtifacts"))
}
