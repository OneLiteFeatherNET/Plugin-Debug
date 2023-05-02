import org.ajoberstar.grgit.Grgit
import java.util.*

plugins {
    java
    `java-library`
    `maven-publish`
    signing
}

group = "dev.themeinerlp"
var baseVersion by extra("1.1.0")
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
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly(rootProject)
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("io.papermc:paperlib:1.0.8")
}

tasks {
    test {
        useJUnitPlatform()
    }
    withJavadocJar()
    withSourcesJar()
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components.findByName("java"))
            groupId = "dev.themeinerlp.plugin-debug"
            artifactId = "bukkit-extension"
            version = "%s%s".format(Locale.ROOT, baseVersion, snapshot)
            pom {
                name.set("Bukkit Extension")
                description.set("The extension for bukkit/paper/spigot for plugin debug")
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
