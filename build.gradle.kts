import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("application")
    `maven-publish`
}

application {
//    mainClassName = "gitMiners.ChangedFilesMinerKt"
    mainClassName = "Main"
}

group = "me.nikolaisv"
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
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.21")
    testImplementation(kotlin("test-junit"))
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "tcd"
            artifactId = "technical-connections"
            version = "0.2"

            from(components["java"])
        }
    }
    val spaceUsername: String by project
    val spacePassword: String by project

    repositories {
        maven {
            url = uri("https://packages.jetbrains.team/maven/p/tcd/maven")
            credentials {
                username = spaceUsername
                password = spacePassword
            }
        }
    }
}
