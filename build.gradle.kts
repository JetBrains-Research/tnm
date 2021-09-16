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
        implementation("org.deeplearning4j:deeplearning4j-core:1.0.0-beta6")
        implementation("org.nd4j:nd4j-native-platform:1.0.0-beta6")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

        testImplementation(kotlin("test-junit"))
    }
}
