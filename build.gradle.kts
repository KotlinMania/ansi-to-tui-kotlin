import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "2.3.20"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.kotlinmania"
version = "0.1.1"

kotlin {
    applyDefaultHierarchyTemplate()

    val xcf = XCFramework("AnsiToTui")

    macosArm64 {
        binaries.framework {
            baseName = "AnsiToTui"
            xcf.add(this)
        }
    }
    macosX64 {
        binaries.framework {
            baseName = "AnsiToTui"
            xcf.add(this)
        }
    }
    linuxX64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("commonMain/ansitotui/src")

            dependencies {
                implementation("io.github.kotlinmania:ratatui-kotlin:0.1.1")
            }
        }

        val commonTest by getting {
            kotlin.srcDir("commonTest/kotlin")
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    jvmToolchain(21)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(group.toString(), "ansi-to-tui-kotlin", version.toString())

    pom {
        name.set("ansi-to-tui-kotlin")
        description.set("Kotlin Multiplatform library for converting ANSI escape sequences to ratatui Text")
        inceptionYear.set("2024")
        url.set("https://github.com/KotlinMania/ansi-to-tui-kotlin")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("sydneyrenee")
                name.set("Sydney Renee")
                email.set("sydney@solace.ofharmony.ai")
                url.set("https://github.com/sydneyrenee")
            }
        }

        scm {
            url.set("https://github.com/KotlinMania/ansi-to-tui-kotlin")
            connection.set("scm:git:git://github.com/KotlinMania/ansi-to-tui-kotlin.git")
            developerConnection.set("scm:git:ssh://github.com/KotlinMania/ansi-to-tui-kotlin.git")
        }
    }
}
