import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
//    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.koin.compiler)
//    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
            implementation(libs.kotlinx.serialization.json)
            api(project.dependencies.platform(libs.supabase.bom))
            api(libs.supabase.auth)
            implementation(libs.supabase.realtime)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.functions)
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.coil.svg)
//            api(libs.sqldelight.coroutines)
            implementation(projects.shared.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
//        jvmMain.dependencies {
//            implementation(libs.ktor.client.okhttp)
//            implementation(libs.sqldelight.jvm.driver)
//        }
//        androidMain.dependencies {
//            implementation(libs.ktor.client.okhttp)
//            implementation(libs.koin.android)
//            implementation(libs.sqldelight.android.driver)
//        }
//        iosMain.dependencies {
//            implementation(libs.ktor.client.darwin)
//            implementation(libs.sqldelight.native.driver)
//        }
//        webMain.dependencies {
//            implementation(libs.ktor.client.js)
//            implementation(libs.sqldelight.js.driver)
//            implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqldelight.get()))
//            implementation (npm("sql.js", "1.14.1"))
//            implementation (devNpm("copy-webpack-plugin", "14.0.0"))
//            implementation (devNpm("webpack", "5.106.2"))
//            implementation(npm("@sqlite.org/sqlite-wasm", "3.51.2-build9"))
//            implementation(npm("os-browserify", "0.3.0"))
//            implementation(npm("path-browserify", "1.0.1"))
//        }
    }
}

android {
    namespace = "io.github.kmpstore.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}