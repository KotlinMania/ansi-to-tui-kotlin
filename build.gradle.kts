plugins {
    kotlin("multiplatform") version "2.2.10"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.kotlinmania"
version = "0.1.0-SNAPSHOT"

kotlin {
    applyDefaultHierarchyTemplate()

    // Native targets
    macosArm64()
    macosX64()
    linuxX64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            // Custom source directory to match current layout
            kotlin.srcDir("commonMain/ansitotui/src")

            dependencies {
                // Depends on ratatui-kotlin for Text, Span, Style types
                implementation("io.github.kotlinmania:ratatui-kotlin:0.1.0")
            }
        }

        val commonTest by getting {
            kotlin.srcDir("commonTest/kotlin")
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val nativeMain by getting {
            dependencies {
                // No additional native dependencies
            }
        }

        val nativeTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

mavenPublishing {
    // Use Sonatype S01 for both SNAPSHOT and Release publications via OSSRH staging
    // This works well with a multi-OS matrix in CI (build native artifacts per OS)
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)
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
