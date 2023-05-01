import org.ajoberstar.grgit.Grgit
import java.util.*

plugins {
    kotlin("jvm") version "1.8.20"
    id("org.ajoberstar.grgit") version "5.2.0"
    `java-library`
}

if (!File("$rootDir/.git").exists()) {
    logger.lifecycle(
        """
    **************************************************************************************
    You need to fork and clone this repository! Don't download a .zip file.
    If you need assistance, consult the GitHub docs: https://docs.github.com/get-started/quickstart/fork-a-repo
    **************************************************************************************
    """.trimIndent()
    ).also { System.exit(1) }
}

group = "dev.themeinerlp"
var baseVersion by extra("1.0.0")
var extension by extra("")
var snapshot by extra("-SNAPSHOT")

ext {
    val git: Grgit = Grgit.open {
        dir = File("$rootDir/.git")
    }
    val revision = git.head().abbreviatedId
    extension = "%s+%s".format(Locale.ROOT, snapshot, revision)
}


version = "%s%s".format(Locale.ROOT, baseVersion, extension)

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    implementation("com.google.code.gson:gson:2.10.1")
    // Test
    testImplementation("net.lingala.zip4j:zip4j:2.11.5")
    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(17)
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}