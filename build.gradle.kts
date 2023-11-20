plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    jcenter()
}

subprojects {

    apply {
        plugin("kotlin")
    }

    repositories {
        jcenter()
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation("org.eclipse.jgit:org.eclipse.jgit:6.3.0.202209071007-r")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
        implementation("org.jetbrains.kotlinx:multik-api:0.1.0")
        implementation("org.jetbrains.kotlinx:multik-default:0.1.0")

        implementation("org.slf4j:slf4j-api:2.0.9")
        implementation("ch.qos.logback:logback-core:1.4.11")

        testImplementation(kotlin("test-junit"))
    }
}
