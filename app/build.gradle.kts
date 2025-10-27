plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-parcelize")  // ✅ AGREGAR: Para @Parcelize
}

android {
    namespace = "com.app.symetrycreed"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.symetrycreed"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // BoM: fija versiones compatibles para todas las libs Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-database-ktx")

    // Firebase (sin versión explícita)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("androidx.activity:activity-ktx:1.8.0")
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    // Credenciales (One Tap)
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Tus otras dependencias
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}