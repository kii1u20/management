import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("dev.hydraulic.conveyor") version "1.12"
}

version = "0.6.0" // a.b.c = major.minor.patch`

kotlin {
    jvm {
        withJava()
    }

    sourceSets {
        val jvmMain by getting {
            kotlin.srcDir(layout.buildDirectory.dir("generated/kotlin"))

            commonMain.dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation("dev.chrisbanes.haze:haze:1.3.0")
                implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.3.1")
                implementation("org.mongodb:bson-kotlinx:5.3.1")
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation("io.github.oshai:kotlin-logging-jvm:7.0.4")
                implementation("ch.qos.logback:logback-classic:1.5.16")
            }
            jvmMain.dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation("dev.chrisbanes.haze:haze:1.3.0")
                implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.3.0")
                implementation("org.mongodb:bson-kotlinx:5.3.0")
                implementation("io.github.oshai:kotlin-logging-jvm:7.0.4")
                implementation("ch.qos.logback:logback-classic:1.5.16")
            }
        }
    }

    sourceSets {
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
    sourceSets.jvmMain.dependencies {
        implementation(kotlin("reflect"))
    }
}

dependencies {
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}

configurations.all {
    attributes {
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
}

tasks.register("generateBuildConfig") {
    doLast {
        val dir = layout.buildDirectory.dir("generated/kotlin/org/w1001/schedule").get().asFile
        dir.mkdirs()
        File(dir, "BuildConfig.kt").writeText("""
            package org.w1001.schedule
            
            object BuildConfig {
                const val VERSION = "${project.version}"
            }
        """.trimIndent())
    }
}

tasks.named("compileKotlinJvm") {
    dependsOn("generateBuildConfig")
}


compose.desktop {
    application {
        mainClass = "org.w1001.schedule.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.w1001.schedule"
            packageVersion = "1.0.0"
        }
        buildTypes.release.proguard {
            isEnabled = false
        }
    }
}
