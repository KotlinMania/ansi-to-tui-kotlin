@file:OptIn(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCacheApi::class)

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.DisableCacheInKotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    kotlin("multiplatform") version "2.3.20"
    id("com.android.kotlin.multiplatform.library") version "9.2.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.kotlinmania"
version = "0.1.4"

kotlin {
    applyDefaultHierarchyTemplate()

    compilerOptions {
        allWarningsAsErrors.set(true)
    }

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
    iosArm64 {
        binaries.framework {
            baseName = "AnsiToTui"
            xcf.add(this)
        }
    }
    iosX64 {
        binaries.framework {
            baseName = "AnsiToTui"
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "AnsiToTui"
            xcf.add(this)
        }
    }
    js {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.all {
            disableNativeCache(
                version = DisableCacheInKotlinVersion.`2_3_20`,
                reason = "Fleeksoft charset/io klib cache crashes Kotlin/Native 2.3.20 during test linking."
            )
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("commonMain/ansitotui/src")

            dependencies {
                implementation("io.github.kotlinmania:ratatui-kotlin:0.1.9") {
                    exclude(group = "com.fleeksoft.io", module = "io-core")
                }
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

kotlin {
    android {
        namespace = "io.github.kotlinmania.ansitotui"
        compileSdk = 34
        minSdk = 24
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }
    }
}

val enableIosSimulatorTests =
    providers.gradleProperty("enableIosSimulatorTests").map { it.toBoolean() }.orElse(false)

tasks.withType<KotlinNativeTest>().configureEach {
    if (!enableIosSimulatorTests.get() && (name == "iosX64Test" || name == "iosSimulatorArm64Test")) {
        enabled = false
    }
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
