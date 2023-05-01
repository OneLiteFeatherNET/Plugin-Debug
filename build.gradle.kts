plugins {
    kotlin("jvm") version "1.8.20"
    `java-library`
}

group = "net.onelitefeather"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}