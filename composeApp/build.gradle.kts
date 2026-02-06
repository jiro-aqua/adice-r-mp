import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation("androidx.appcompat:appcompat:1.7.1")
            implementation("androidx.navigation:navigation-fragment-ktx:2.9.6")
            implementation("androidx.navigation:navigation-ui-ktx:2.9.6")
            implementation("androidx.preference:preference-ktx:1.2.1")

            // Compose Core
            implementation( "androidx.compose.ui:ui:1.10.2")
            implementation ("androidx.compose.ui:ui-graphics:1.10.2")
            implementation ("androidx.compose.ui:ui-tooling-preview:1.10.2")
            implementation ("androidx.compose.material3:material3:1.4.0")
            implementation ("androidx.compose.material:material-icons-extended:1.7.8")

            // Compose Activity Integration
            implementation ("androidx.activity:activity-compose:1.10.1")

            // Compose Navigation
            implementation ("androidx.navigation:navigation-compose:2.9.6")
            implementation("com.squareup.okhttp3:okhttp:5.3.2")
            implementation("com.squareup.okio:okio:3.9.0")
            // okhttp
            implementation("com.squareup.okhttp3:okhttp:5.3.2")
            implementation("com.squareup.okio:okio:3.9.0")        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

android {
    namespace = "jp.gr.aqua.adice"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "jp.gr.aqua.adicer.mp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "jp.gr.aqua.adicer.mp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "jp.gr.aqua.adicer.mp"
            packageVersion = "1.0.0"
        }
    }
}
