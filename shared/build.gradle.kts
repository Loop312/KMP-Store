import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.koin.compiler)
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
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.coil.svg)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        webMain.dependencies {
            implementation(libs.ktor.client.js)
        }
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

val props = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        println("Found local.properties")
        load(propertiesFile.inputStream())
    }
}

buildkonfig {
    packageName = "io.github.kmpstore"
    // objectName = "YourAwesomeConfig"
    // exposeObjectWithName = "YourAwesomePublicConfig"

    defaultConfigs {
        // Syntax: buildConfigField(Type, "VariableName", "Value")
        buildConfigField(
            STRING,
            "SUPABASE_URL",
            props.getProperty("SUPABASE_URL") ?: ""
        )
        buildConfigField(
            STRING,
            "SUPABASE_KEY",
            props.getProperty("SUPABASE_KEY") ?: ""
        )
        buildConfigField(
            STRING,
            "STORE_NAME",
            props.getProperty("STORE_NAME") ?: ""
        )
    }
}