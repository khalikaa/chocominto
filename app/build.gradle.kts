import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
}


android {
    namespace = "com.example.chocominto"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
    }

    val localProperties = Properties().apply {
        load(FileInputStream(rootProject.file("local.properties")))
    }

    val apiKey = localProperties.getProperty("api_key") ?: ""


    defaultConfig {
        applicationId = "com.example.chocominto"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.glide)
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}