plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    compileSdkVersion(project.ext.androidCompileSdk)

    defaultConfig {
        minSdkVersion(project.ext.androidMinSdk)
        targetSdkVersion(project.ext.androidTargetSdk)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles = "consumer-rules.pro"
    }

    buildTypes {
        getByName("debug") {
            testCoverageEnabled = project.hasProperty("coverage")
        }
        getByName("release") {
            minifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
        implementation("androidx.core:core-ktx:1.6.0")

        api(platform("com.google.firebase:firebase-bom:28.2.1"))
        api("com.google.firebase:firebase-analytics-ktx")
        api("com.google.firebase:firebase-crashlytics")

        implementation("com.google.dagger:dagger:2.31.2")
        kapt("com.google.dagger:dagger-compiler:2.31.2")
        implementation("com.google.dagger:dagger-android-support:2.31.2")
        kapt("com.google.dagger:dagger-android-processor:2.31.2")
        compileOnly("com.squareup.inject:assisted-inject-annotations-dagger2:0.3.2")
        kapt("com.squareup.inject:assisted-inject-processor-dagger2:0.3.2")
        implementation("com.google.dagger:hilt-android:2.38.1")
        kapt("com.google.dagger:hilt-android-compiler:2.38.1")
        implementatio("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
        kapt("androidx.hilt:hilt-compiler:1.0.0")

        testImplementation("junit:junit:4.13.2")
        testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.0.0")
        testImplementation("org.robolectric:robolectric:4.6.1")
        testImplementation("com.google.truth:truth:1.1.3")
    }
}