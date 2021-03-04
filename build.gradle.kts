import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("application")
    `maven-publish`
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

group = "me.xxx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-html-assembly:0.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.1")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.9.0.202009080501-r")
    // TODO: to big mb change
    implementation("org.deeplearning4j:deeplearning4j-core:1.0.0-beta6")
    implementation("org.nd4j:nd4j-native-platform:1.0.0-beta6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("com.github.ajalt.clikt:clikt:3.1.0")
    testImplementation(kotlin("test-junit"))
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    useJUnit()
    maxHeapSize = "3G"
}
