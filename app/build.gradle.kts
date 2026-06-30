import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

// Reads secrets in priority order: local.properties -> Gradle property -> environment.
// Returns "" when unset, in which case the app falls back to the free, no-key providers.
val secretsProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun secret(key: String): String =
    secretsProps.getProperty(key)
        ?: providers.gradleProperty(key).orNull
        ?: System.getenv(key)
        ?: ""

android {
    namespace = "com.aitracker.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aitracker.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Optional keyed providers. Leave blank to use the free Yahoo Finance /
        // Google News fallbacks. Set in local.properties, e.g.:
        //   FINNHUB_API_KEY=xxxxx
        //   NEWS_API_KEY=yyyyy
        buildConfigField("String", "FINNHUB_API_KEY", "\"${secret("FINNHUB_API_KEY")}\"")
        buildConfigField("String", "NEWS_API_KEY", "\"${secret("NEWS_API_KEY")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit)

    debugImplementation(libs.androidx.ui.tooling)
}
