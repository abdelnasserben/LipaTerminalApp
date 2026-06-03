plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.lipa.terminal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lipa.terminal"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // Environment selection, mirroring the Lipa customer/agent apps:
    //   -PENV=local  -> talks to a backend on the dev machine (default)
    //   -PENV=prod   -> talks to the production API
    // An explicit -PKOMOPAY_API_BASE_URL=... always wins over the per-env default.
    val komopayEnvProp = ((project.findProperty("ENV") as String?) ?: "local").trim().lowercase()
    val komopayLocalUrl = "http://localhost:8080"
    val komopayProdUrl = "https://api.lipa.km"
    val komopayBaseUrlOverride = (project.findProperty("KOMOPAY_API_BASE_URL") as String?)?.trim().orEmpty()
    val komopayBaseUrlProp = when {
        komopayBaseUrlOverride.isNotEmpty() -> komopayBaseUrlOverride
        komopayEnvProp == "prod" || komopayEnvProp == "production" -> komopayProdUrl
        else -> komopayLocalUrl
    }

    buildTypes {
        debug {
            buildConfigField("String", "KOMOPAY_API_BASE_URL", "\"$komopayBaseUrlProp\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "KOMOPAY_API_BASE_URL", "\"$komopayBaseUrlProp\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        jvmToolchain(21)
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
