import org.gradle.api.JavaVersion

import org.gradle.api.JavaVersion.VERSION_1_8

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.unichamba"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.unichamba"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xlint:-options"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = VERSION_1_8
        targetCompatibility = VERSION_1_8

    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding=true
    }
    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
    }
    buildToolsVersion = "34.0.0"
    ndkVersion = "26.2.11394342"
}


dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.compose.animation:animation-graphics-android:1.6.7")
    implementation("androidx.core:core-animation:1.0.0")
    testImplementation("junit:junit:4.13.2")
    implementation("com.google.firebase:firebase-analytics-ktx")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.firebase:firebase-analytics:22.0.1")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-bom:33.1.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation ("com.google.firebase:firebase-firestore:25.0.0")
    implementation ("com.google.firebase:firebase-storage:21.0.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")


}