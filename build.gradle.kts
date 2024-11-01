import org.ajoberstar.grgit.Grgit
import java.util.*

plugins {
    java
    id("org.ajoberstar.grgit") version "5.3.0"
    `java-library`
    `maven-publish`
    signing
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
var baseVersion by extra("1.1.0")
var extension by extra("")
var snapshot by extra("")

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
}

tasks {
    test {
        useJUnitPlatform()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withJavadocJar()
    withSourcesJar()
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components.findByName("java"))
            groupId = "dev.themeinerlp"
            artifactId = "plugin-debug"
            version = "%s%s".format(Locale.ROOT, baseVersion, snapshot)
            pom {
                name.set("Plugin debug")
                description.set("A simple library to upload plugin debugs")
                url.set("https://github.com/OneLiteFeatherNET/Plugin-Debug")
                licenses {
                    license {
                        name.set("AGPL-3.0")
                        url.set("https://github.com/OneLiteFeatherNET/Plugin-Debug/blob/main/LICENSE")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/OneLiteFeatherNET/Plugin-Debug/issues")
                }
                developers {
                    developer {
                        id.set("TheMeinerLP")
                        name.set("Phillipp Glanz")
                        email.set("p.glanz@madfix.me")
                    }
                }
                scm {
                    connection.set("scm:git@github.com:OneLiteFeatherNET/Plugin-Debug.git")
                    developerConnection.set("scm:git@github.com:OneLiteFeatherNET/Plugin-Debug.git")
                    url.set("https://github.com/OneLiteFeatherNET/Plugin-Debug")
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = if (version.toString().contains("SNAPSHOT")) uri(snapshotsRepoUrl) else uri(releasesRepoUrl)
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
