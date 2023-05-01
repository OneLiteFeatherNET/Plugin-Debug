import org.ajoberstar.grgit.Grgit
import java.util.*

plugins {
    kotlin("jvm") version "1.8.21"
    `java-library`
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.8.10"
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
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly(rootProject)
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("io.papermc:paperlib:1.0.8")
    testImplementation(platform("org.junit:junit-bom:5.9.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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

val sourceJar by tasks.register<Jar>("kotlinJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}
val dokkaJavadocJar by tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(rootProject.tasks.dokkaHtml)
    from(rootProject.tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-docs")
}

val dokkaHtmlJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(rootProject.tasks.dokkaJavadoc)
    from(rootProject.tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components.findByName("java"))
            groupId = "dev.themeinerlp.plugin-debug"
            artifactId = "bukkit-extension"
            version = "%s%s".format(Locale.ROOT, baseVersion, snapshot)
            artifact(dokkaJavadocJar)
            artifact(dokkaHtmlJar)
            artifact(sourceJar)
            pom {
                name.set("Bukkit Extension")
                description.set("The extension for bukkit/paper/spigot fir plugin debug")
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
