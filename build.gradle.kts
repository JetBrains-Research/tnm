import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("application")
}

application {
    mainClassName = "gitMiners.ChangedFilesMinerKt"
}

group = "me.nikolaisv"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.9.0.202009080501-r")
    // TODO: to big mb change
    implementation("org.deeplearning4j:deeplearning4j-core:1.0.0-beta6")
    implementation("org.nd4j:nd4j-native-platform:1.0.0-beta6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    testImplementation(kotlin("test-junit"))
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}