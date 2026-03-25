plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myapplication"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes.add("META-INF/LGPL2.1")
            excludes.add("META-INF/kotlin-jupyter-libraries/libraries.json")
            excludes.add("META-INF/thirdparty-LICENSE")
            excludes.add("META-INF/AL2.0")
            excludes.add("META-INF/DEPENDENCIES")


            excludes.add("arrow-git.properties")
//            excludes.add("META-INF/LICENSE")
//            excludes.add("META-INF/NOTICE")

        }
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
        compose = true
    }
}

dependencies {
//    val vicoVersion = "2.0.0-alpha.22" // Използвайте една и съща версия за всички

    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1");
    implementation("io.coil-kt:coil-compose:2.5.0")


    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.compose.ui:ui-text-google-fonts:1.10.5")
    implementation("io.github.ehsannarmani:compose-charts:0.2.5")

// За viewModelScope (ако ползваш ViewModel - силно препоръчително
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

// За конвертиране на JSON
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}