plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.official.recipesnap"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.official.recipesnap"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resValue("string", "gemini_api_key", "\"${project.properties["GEMINI_API_KEY"]}\"")

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
    }
}

dependencies {

    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)

        // Compose BOM and core UI
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3) // Use material or material3 depending on theme

        // Coil for loading selected image preview
        implementation("io.coil-kt.coil3:coil-compose:3.2.0")
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

        //Gemini API
        //implementation("com.google.ai.client:generativeai:0.5.1")

        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")

        implementation("io.ktor:ktor-client-core:2.3.7")
        implementation("io.ktor:ktor-client-cio:2.3.7")
        implementation("io.ktor:ktor-client-okhttp:2.3.7") // Optional
        implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
        implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    }

}