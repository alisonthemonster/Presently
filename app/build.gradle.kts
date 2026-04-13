import org.gradle.api.GradleException
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.play.publisher)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.screenshotbot)
    alias(libs.plugins.hilt.android)
    id("com.google.android.gms.oss-licenses-plugin")
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

apply(from = "../gradle/dependency_graph.gradle")

val localProperties = loadLocalProperties()
val missingDropboxKey = "missing_local_key"
val appVersionName = listOf(
    libs.versions.appVersionMajor.get(),
    libs.versions.appVersionMinor.get(),
    libs.versions.appVersionPatch.get(),
).joinToString(".")
val supportedAppLanguages = listOf(
    "en",
    "af",
    "ar",
    "de",
    "es",
    "fi",
    "fr",
    "hr",
    "it",
    "nl",
    "pl",
    "pt",
    "pt-rBR",
    "ro",
    "ru",
    "sk",
    "tr",
    "zh-rHK",
)

android {
    namespace = "journal.gratitude.com.gratitudejournal"
    compileSdk = libs.versions.compileSdk.get().toInt()

    signingConfigs {
        create("release") {
            val releaseStoreFile = getReleaseStoreFile()
            if (releaseStoreFile != null) {
                storeFile = file(releaseStoreFile)
                storePassword = getRequiredReleaseSecret("RELEASE_STORE_PASSWORD")
                keyAlias = getRequiredReleaseSecret("RELEASE_KEY_ALIAS")
                keyPassword = getRequiredReleaseSecret("RELEASE_KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "journal.gratitude.com.gratitudejournal"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.appVersionCode.get().toInt()
        versionName = appVersionName

        testInstrumentationRunner = "journal.gratitude.com.gratitudejournal.testUtils.AppCustomTestRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }

        val dropboxKey = getDropboxKey()
        buildConfigField("String", "DROPBOX_APP_KEY", "\"${dropboxKey}\"")
        manifestPlaceholders["dropboxAppKey"] = dropboxKey
    }

    buildFeatures {
        dataBinding = true //TODO are we still using this?
        viewBinding = true
        compose = true
    }

    androidResources {
        localeFilters += supportedAppLanguages
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    packaging {
        resources {
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
        }
    }

    buildTypes {
        getByName("debug") {
            isTestCoverageEnabled = project.hasProperty("coverage")
            versionNameSuffix = "-DEBUG"
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val releaseStoreFile = getReleaseStoreFile()
            if (releaseStoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }


    testOptions {
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
        unitTests.all {
            it.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
            if (project.hasProperty("screenshot")) {
                it.useJUnit {
                    includeCategories("journal.gratitude.com.gratitudejournal.testUtils.ScreenshotTest")
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// Robolectric requires JDK 21 to emulate Android SDK 36 (the android-all jars for
// SDK 36 are compiled with Java 21). Only the unit-test JVM uses 21; app compilation
// and bytecode still target Java 17 (see compileOptions/kotlinOptions above).
tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    )
}

play {
    defaultToAppBundles.set(true)

    val playServiceAccountFile = getPlayServiceAccountFile()
    if (playServiceAccountFile != null) {
        serviceAccountCredentials.set(file(playServiceAccountFile))
    }
}

tasks.register("verifyReleaseConfig") {
    group = "verification"
    description = "Checks release-only secrets and files before building a release."

    doLast {
        val missing = mutableListOf<String>()

        if (!project.file("google-services.json").isFile) {
            missing += "app/google-services.json"
        }

        if (getDropboxKey() == missingDropboxKey) {
            missing += "DROPBOX_APP_KEY (or DROPBOX_KEY in local.properties)"
        }

        val releaseStoreFile = getReleaseStoreFile()
        if (releaseStoreFile == null) {
            missing += "RELEASE_STORE_FILE"
        } else {
            if (getReleaseSecret("RELEASE_STORE_PASSWORD") == null) {
                missing += "RELEASE_STORE_PASSWORD"
            }
            if (getReleaseSecret("RELEASE_KEY_ALIAS") == null) {
                missing += "RELEASE_KEY_ALIAS"
            }
            if (getReleaseSecret("RELEASE_KEY_PASSWORD") == null) {
                missing += "RELEASE_KEY_PASSWORD"
            }
        }

        if (missing.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Release inputs are missing:")
                    missing.forEach { appendLine(" - $it") }
                }.trimEnd()
            )
        }
    }
}

tasks.register("verifyPlayPublisherConfig") {
    group = "verification"
    description = "Checks Play publishing credentials before upload or promotion."

    doLast {
        val playServiceAccountFile = getPlayServiceAccountFile()
        if (playServiceAccountFile == null) {
            throw GradleException("Missing required Play publishing secret: PLAY_SERVICE_ACCOUNT_FILE")
        }

        if (!file(playServiceAccountFile).isFile) {
            throw GradleException("Play publishing credentials file does not exist: $playServiceAccountFile")
        }
    }
}

listOf("bundleRelease", "assembleRelease").forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        dependsOn("verifyReleaseConfig")
    }
}

listOf("publishReleaseBundle", "publishBundle").forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        dependsOn("verifyReleaseConfig", "verifyPlayPublisherConfig")
    }
}

listOf("promoteReleaseArtifact", "promoteArtifact").forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        dependsOn("verifyPlayPublisherConfig")
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.google.play.feature.delivery)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    kapt(libs.androidx.room.compiler)

    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    kapt(libs.androidx.lifecycle.compiler)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.lottie.compose)

    implementation(libs.three.ten.abp)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.material)
    implementation(libs.google.play.services.auth)
    implementation(libs.google.play.services.oss.licenses)
    implementation(libs.kizitonwose.calendar.compose)
    implementation(libs.dropbox.core.sdk)
    implementation(libs.dropbox.android.sdk)
    implementation(libs.apache.text)
    implementation(libs.apache.csv)

    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxbinding)

    implementation(libs.mavericks)
    implementation(libs.mavericks.mocking)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics)

    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
    implementation(libs.dagger.android.support)
    kapt(libs.dagger.android.processor)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.work)

    testImplementation(libs.junit4)
    testImplementation(libs.three.ten.bp) {
        exclude(group = "com.jakewharton.threetenabp", module = "threetenabp")
    }
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mavericks.testing)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.hamcrest)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.contrib)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.kotlin.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.three.ten.bp)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mavericks.testing)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    kaptAndroidTest(libs.hilt.android.compiler)
    debugImplementation(libs.androidx.test.core.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestUtil(libs.androidx.test.orchestrator)
}

roborazzi {
    outputDir.set(file("src/test/screenshots"))
}

fun getDropboxKey(): String {
    return getReleaseSecret("DROPBOX_APP_KEY")
        ?: getReleaseSecret("DROPBOX_KEY")
        ?: missingDropboxKey
}

fun getReleaseStoreFile(): String? {
    return getReleaseSecret("RELEASE_STORE_FILE")
}

fun getPlayServiceAccountFile(): String? {
    return getReleaseSecret("PLAY_SERVICE_ACCOUNT_FILE")
}

fun getRequiredReleaseSecret(name: String): String {
    return getReleaseSecret(name)
        ?: throw GradleException("Missing required release secret: $name")
}

fun getReleaseSecret(name: String): String? {
    return System.getenv(name)?.takeIf { it.isNotBlank() }
        ?: localProperties.getProperty(name)?.takeIf { it.isNotBlank() }
}

fun loadLocalProperties(): Properties {
    val localPropertiesFile = rootProject.file("local.properties")
    return Properties().apply {
        if (localPropertiesFile.isFile) {
            localPropertiesFile.inputStream().use(::load)
        }
    }
}
