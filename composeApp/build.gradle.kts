import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.gms)
    alias(libs.plugins.ktlint)
}

kotlin {
    // Enable Kotlin 2.3.0 unused return value checker to catch silently dropped Results
    compilerOptions {
        freeCompilerArgs.add("-Xwarn-unused-return-value")
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static =
                            (static ?: mutableListOf()).apply {
                                // Serve sources to debug inside browser
                                add(rootDirPath)
                                add(projectDirPath)
                            }
                    }
            }
        }
        binaries.executable()
    }

    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.coil.network.okhttp)
            implementation(libs.firebase.common.ktx)
            implementation(libs.firebase.firestore.ktx)
            implementation(libs.firebase.auth.ktx)
            implementation(libs.firebase.storage.ktx)
            // Override Firebase Auth to fix session persistence bug (firebase-android-sdk#7111)
            // GitLive's 2.4.0 uses Firebase Auth 23.2.1 with persistence bug, manually pin to 24.0.0+
            implementation("com.google.firebase:firebase-auth:24.0.0")
            implementation(libs.compose.material3)
            implementation(libs.android.material)
            implementation(libs.play.services.auth)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.firebase.common.ktx)
            implementation(libs.firebase.firestore.ktx)
            implementation(libs.firebase.auth.ktx)
            implementation(libs.firebase.storage.ktx)
        }

        wasmJsMain.dependencies {
            implementation(libs.bundles.ktor.wasm)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.bundles.ktor)
            implementation(libs.bundles.coil)
            implementation(libs.material.icons)
            implementation(libs.kotlinx.datetime)
            implementation(libs.placeholder)
            implementation(libs.imagepickerkmp)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.marquis.zorroexpense"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.marquis.zorroexpense"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        val localPropertiesFile = rootProject.file("local.properties")
        val localProperties = Properties()
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
            localProperties.forEach { (key, value) ->
                project.ext.set(key as String, value)
            }
        }
        create("release") {
            storeFile = file(project.property("RELEASE_STORE_FILE").toString())
            storePassword = project.property("RELEASE_STORE_PASSWORD").toString()
            keyAlias = project.property("RELEASE_KEY_ALIAS").toString()
            keyPassword = project.property("RELEASE_KEY_PASSWORD").toString()
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

ktlint {
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }

    // Set max line length to 200
    additionalEditorconfig =
        mapOf(
            "max_line_length" to "200",
        )
}
