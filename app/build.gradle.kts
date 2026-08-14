import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.official.recipesnap"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.official.recipesnap"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        val apiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        val spoonacularApiKey = localProperties.getProperty("SPOONACULAR_API_KEY") ?: ""
        
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")
        buildConfigField("String", "SPOONACULAR_API_KEY", "\"$spoonacularApiKey\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
        implementation("androidx.core:core-splashscreen:1.2.0")
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)

        // Compose BOM and core UI
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3) // Use material or material3 depending on theme
        implementation(libs.androidx.material.icons.extended)

        // Coil for loading selected image preview and network images
        implementation("io.coil-kt.coil3:coil-compose:3.2.0")
        implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0")
        implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
        implementation("com.google.firebase:firebase-analytics")
        // Testing
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)

        // Debug tools
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)

        // Gemini API
        implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
        
        // ViewModel for Compose
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

        // Markdown renderer
        implementation("io.noties.markwon:core:4.6.2")
        
        // JSON Serialization
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
        
        // Retrofit & OkHttp
        implementation(libs.retrofit)
        implementation(libs.retrofit.kotlinx.serialization)
        implementation(libs.okhttp)
        
        // Health Connect
        implementation("androidx.health.connect:connect-client:1.1.0-alpha07")
}