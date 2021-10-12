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
        implementation("org.eclipse.jgit:org.eclipse.jgit:5.12.0.202106070339-r")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
        implementation("org.jetbrains.kotlinx:multik-api:0.1.0")
        implementation("org.jetbrains.kotlinx:multik-default:0.1.0")

        testImplementation(kotlin("test-junit"))
    }
}
