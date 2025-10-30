plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    // id("com.google.dagger.hilt.android")
    // id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.appshub.sipcalculator_financeplanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.appshub.sipcalculator_financeplanner"
        minSdk = 26
        targetSdk = 35
        versionCode = 17
        versionName = "1.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // Enable support for 16 KB page sizes
        ndk {
            debugSymbolLevel = "SYMBOL_TABLE"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            // Disable Firebase Analytics for debug builds
            manifestPlaceholders["firebase_analytics_collection_enabled"] = false
            manifestPlaceholders["firebase_analytics_collection_deactivated"] = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Enable Firebase Analytics for release builds
            manifestPlaceholders["firebase_analytics_collection_enabled"] = true
            manifestPlaceholders["firebase_analytics_collection_deactivated"] = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
    
    // Support for 16 KB memory page sizes (Android 15+)
    bundle {
        abi {
            enableSplit = true
        }
    }
    
    // NDK configuration for 16 KB page size support
    ndkVersion = "26.1.10909125"
}

dependencies {
    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    
    // Google Mobile Ads SDK (AdMob) - Updated for Families Policy Compliance
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    
    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    
    // Compose BOM - Compatible version for AGP 8.2.2 and API 35
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // ViewModel & Architecture
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-process:2.8.4")
    
    // Hilt Dependency Injection - Temporarily commented out to avoid KAPT issues
    // implementation("com.google.dagger:hilt-android:2.48")
    // implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    // kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Room Database - Temporarily commented out to avoid KAPT issues
    // implementation("androidx.room:room-runtime:2.6.1")
    // implementation("androidx.room:room-ktx:2.6.1")
    // kapt("androidx.room:room-compiler:2.6.1")
    
    // Charts Library
    implementation("co.yml:ycharts:2.1.0")
    
    // Number formatting
    implementation("com.ibm.icu:icu4j:71.1")
    
    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.2")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}