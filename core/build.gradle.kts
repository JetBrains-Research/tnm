import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    `maven-publish`
}

group = "me.xxx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

val testConfig = configurations.create("testArtifacts") {
    extendsFrom(configurations["testCompile"])
}

tasks.register("testJar", Jar::class.java) {
    dependsOn("testClasses")
    classifier += "test"
    from(sourceSets["test"].output)
}

artifacts {
    add("testArtifacts", tasks.named<Jar>("testJar"))
}

dependencies {
    api(kotlin("stdlib-js"))
    api("org.jetbrains.kotlinx:kotlinx-html-assembly:0.7.1")
    api("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.1")
    api("org.eclipse.jgit:org.eclipse.jgit:5.9.0.202009080501-r")
    // TODO: to big mb change
    api("org.deeplearning4j:deeplearning4j-core:1.0.0-beta6")
    api("org.nd4j:nd4j-native-platform:1.0.0-beta6")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    api("com.github.ajalt.clikt:clikt:3.1.0")
    testImplementation(kotlin("test-junit"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    useJUnit()
    maxHeapSize = "3G"
}
