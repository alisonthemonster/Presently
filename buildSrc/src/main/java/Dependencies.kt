object Versions {
    const val COMPILE_SDK = 33
    const val TARGET_SDK = 31
    const val MIN_SDK = 23

    const val APP_VERSION_CODE = 86

    const val MAJOR = 1
    const val MINOR = 20
    const val PATCH = 0

    const val KOTLIN = "1.8.20" //https://kotlinlang.org/docs/releases.html#release-details
    const val KOTLIN_COROUTINES = "1.5.0" //https://github.com/Kotlin/kotlinx.coroutines/releases/
    const val KOTLIN_COROUTINES_TEST = "1.3.2"
    const val ANDROIDX_CORE_KTX = "1.6.0" //https://developer.android.com/jetpack/androidx/releases/core
    const val ANDROIDX_COMPAT = "1.6.1" //https://developer.android.com/jetpack/androidx/releases/appcompat
    const val ANDROIDX_CONSTRAINT = "2.0.4" //https://developer.android.com/jetpack/androidx/releases/constraintlayout
    const val ANDROIDX_CONSTRAINT_COMPOSE = "1.0.1" //https://developer.android.com/jetpack/androidx/releases/constraintlayout
    const val ANDROIDX_LIFECYCLE = "2.5.1" //https://developer.android.com/jetpack/androidx/releases/lifecycle
    const val ANDROIDX_FRAGMENT = "1.6.0-beta01" //https://developer.android.com/jetpack/androidx/releases/fragment
    const val ANDROIDX_BIOMETRIC = "1.2.0-alpha04" //https://developer.android.com/jetpack/androidx/releases/biometric
    const val ANDROIDX_TEST_JUNIT = "1.1.2"
    const val ANDROIDX_WORK = "2.7.1" //https://developer.android.com/jetpack/androidx/releases/work
    const val ANDROIDX_ROOM = "2.4.0-alpha03" //https://developer.android.com/jetpack/androidx/releases/room
    const val ANDROIDX_TEST_CORE_KTX = "1.3.0"
    const val ANDROIDX_TEST_ESPRESSO = "3.3.0"
    const val ANDROIDX_TEST_RULES = "1.3.0"
    const val ANDROIDX_TEST_RUNNER = "1.3.0"
    const val ANDROIDX_TEST_UIAUTOMATOR = "2.2.0"
    const val ANDROIDX_PREF_KTX = "1.1.1"
    const val ANDROIDX_ARCH_CORE = "2.1.0" //https://developer.android.com/jetpack/androidx/releases/arch-core
    const val ANDROIDX_DATA_STORE = "1.0.0" //https://developer.android.com/jetpack/androidx/releases/datastore
    const val FIREBASE = "28.2.1" //https://firebase.google.com/support/release-notes/android
    const val DAGGER = "2.45" //https://github.com/google/dagger/releases
    const val HILT_ANDROID = "1.0.0" //https://developer.android.com/jetpack/androidx/releases/hilt
    const val HILT_NAVIGATION_ANDROID = "1.1.0-alpha01" //https://developer.android.com/jetpack/androidx/releases/hilt
    const val JUNIT = "4.13.2"
    const val DROPBOX_SDK = "5.1.1" //https://github.com/dropbox/dropbox-sdk-java/releases
    const val PLAY_CORE = "1.10.0"
    const val MOCKITO_KOTLIN = "4.1.0"
    const val MOCKITO_ANDROID = "2.23.0"
    const val ROBOLECTRIC = "4.6.1" //https://github.com/robolectric/robolectric/releases/
    const val TRUTH = "1.1.3"
    const val THREE_TEN_ABP = "1.3.1" //https://github.com/JakeWharton/ThreeTenABP/tags
    const val MATERIAL = "1.4.0-rc01"
    const val MATERIAL_3 = "1.1.0-rc01" //https://developer.android.com/jetpack/androidx/releases/compose-material3
    const val OSS_LICENSES = "17.0.0"
    const val COMPACT_CAL_VIEW = "3.0.0"
    const val APACHE = "1.6"
    const val ESPRESSO = "3.3.0"
    const val TEST_ORCHESTRATOR = "1.0.2"
    const val COMPOSE = "1.1.1"
    const val COMPOSE_ACTIVITY = "1.4.0"
    const val ACCOMPANIST = "0.24.5-alpha"
    const val KOTLIN_IMMUTABLE = "0.3.5"
    const val DESUGAR_JDK = "1.2.2"
    const val KOTLIN_DATETIME = "0.4.0" //https://github.com/Kotlin/kotlinx-datetime/tags
}

object Libraries {
   const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}"
   const val kotlin_coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.KOTLIN_COROUTINES}"
   const val androidx_core_ktx = "androidx.core:core-ktx:${Versions.ANDROIDX_CORE_KTX}"
   const val firebase_bom = "com.google.firebase:firebase-bom:${Versions.FIREBASE}"
   const val firebase_analytics_ktx = "com.google.firebase:firebase-analytics-ktx"
   const val firebase_crashlytics = "com.google.firebase:firebase-crashlytics"
   const val dagger = "com.google.dagger:dagger:${Versions.DAGGER}"
   const val dagger_compiler = "com.google.dagger:dagger-compiler:${Versions.DAGGER}"
   const val dagger_android_support = "com.google.dagger:dagger-android-support:${Versions.DAGGER}"
   const val dagger_android_processor = "com.google.dagger:dagger-android-processor:${Versions.DAGGER}"
   const val hilt = "com.google.dagger:hilt-android:${Versions.DAGGER}"
   const val hilt_compiler = "com.google.dagger:hilt-compiler:${Versions.DAGGER}"
   const val hilt_android_compiler = "androidx.hilt:hilt-compiler:${Versions.HILT_ANDROID}"
   const val androidx_compat = "androidx.appcompat:appcompat:${Versions.ANDROIDX_COMPAT}"
   const val androidx_constraint_layout = "androidx.constraintlayout:constraintlayout:${Versions.ANDROIDX_CONSTRAINT}"
   const val androidx_fragment = "androidx.fragment:fragment-ktx:${Versions.ANDROIDX_FRAGMENT}"
   const val androidx_lifecycle_compiler = "androidx.lifecycle:lifecycle-compiler:${Versions.ANDROIDX_LIFECYCLE}"
   const val androidx_viewmodel_ktx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.ANDROIDX_LIFECYCLE}"
   const val androidx_lifecycle_runtime_ktx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.ANDROIDX_LIFECYCLE}"
   const val androidx_preference_ktx = "androidx.preference:preference-ktx:${Versions.ANDROIDX_PREF_KTX}"
   const val androidx_work_runtime_ktx = "androidx.work:work-runtime-ktx:${Versions.ANDROIDX_WORK}"
   const val androidx_hilt_work = "androidx.hilt:hilt-work:${Versions.HILT_ANDROID}"
   const val androidx_biometric = "androidx.biometric:biometric:${Versions.ANDROIDX_BIOMETRIC}"
   const val androidx_room_ktx = "androidx.room:room-ktx:${Versions.ANDROIDX_ROOM}"
   const val androidx_room_runtime = "androidx.room:room-runtime:${Versions.ANDROIDX_ROOM}"
   const val androidx_room_compiler = "androidx.room:room-compiler:${Versions.ANDROIDX_ROOM}"
   const val androidx_data_store = "androidx.datastore:datastore-preferences:${Versions.ANDROIDX_DATA_STORE}"
   const val three_ten_abp = "com.jakewharton.threetenabp:threetenabp:${Versions.THREE_TEN_ABP}"
   const val dropbox_sdk = "com.dropbox.core:dropbox-core-sdk:${Versions.DROPBOX_SDK}"
   const val play_core = "com.google.android.play:core:${Versions.PLAY_CORE}"
   const val material = "com.google.android.material:material:${Versions.MATERIAL}"
   const val play_services_oss_licenses = "com.google.android.gms:play-services-oss-licenses:${Versions.OSS_LICENSES}"
   const val apache_text = "org.apache.commons:commons-text:${Versions.APACHE}"
   const val apache_csv = "org.apache.commons:commons-csv:${Versions.APACHE}"
   const val compose_activities = "androidx.activity:activity-compose:${Versions.COMPOSE_ACTIVITY}"
   const val compose_ui = "androidx.compose.ui:ui:${Versions.COMPOSE}"
   const val compose_material = "androidx.compose.material:material:${Versions.COMPOSE}"
   const val compose_material3 = "androidx.compose.material3:material3:${Versions.MATERIAL_3}"
   const val compose_animation = "androidx.compose.animation:animation:${Versions.COMPOSE}"
   const val compose_tooling = "androidx.compose.ui:ui-tooling:${Versions.COMPOSE}"
   const val compose_interop_view_binding = "androidx.compose.ui:ui-viewbinding:${Versions.COMPOSE}"
   const val viewmodel_savedstate = "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.ANDROIDX_LIFECYCLE}"
   const val compose_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.ANDROIDX_LIFECYCLE}"
   const val compose_hilt_navigation = "androidx.hilt:hilt-navigation-compose:${Versions.HILT_NAVIGATION_ANDROID}"
   const val compose_navigation = "androidx.navigation:navigation-compose:${Versions.COMPOSE}"
   const val compose_accompanist_navigation_animation = "com.google.accompanist:accompanist-navigation-animation:${Versions.ACCOMPANIST}"
   const val compose_accompanist_systemuicontroller = "com.google.accompanist:accompanist-systemuicontroller:${Versions.ACCOMPANIST}"
   const val compose_accompanist_insetsui = "com.google.accompanist:accompanist-insets-ui:${Versions.ACCOMPANIST}"
   const val compose_constraint = "androidx.constraintlayout:constraintlayout-compose:${Versions.ANDROIDX_CONSTRAINT_COMPOSE}"
   const val kotlin_immutable = "org.jetbrains.kotlinx:kotlinx-collections-immutable:${Versions.KOTLIN_IMMUTABLE}"
   const val desugar = "com.android.tools:desugar_jdk_libs:${Versions.DESUGAR_JDK}"
   const val kotlinx_datetime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.KOTLIN_DATETIME}"
}

object TestLibraries {
    const val junit = "junit:junit:${Versions.JUNIT}"
    const val kotlin_test_junit = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.KOTLIN}"
    const val kotlin_coroutines_test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.KOTLIN_COROUTINES_TEST}"
    const val mockito_kotlin = "org.mockito.kotlin:mockito-kotlin:${Versions.MOCKITO_KOTLIN}"
    const val mockito_android = "org.mockito:mockito-android:${Versions.MOCKITO_ANDROID}"
    const val robolectric = "org.robolectric:robolectric:${Versions.ROBOLECTRIC}"
    const val truth = "com.google.truth:truth:${Versions.TRUTH}"
    const val fragment_testing = "androidx.fragment:fragment-testing:${Versions.ANDROIDX_FRAGMENT}"
    const val androidx_arch_testing = "androidx.arch.core:core-testing:${Versions.ANDROIDX_ARCH_CORE}"
    const val androidx_test_junit = "androidx.test.ext:junit:${Versions.ANDROIDX_TEST_JUNIT}"
    const val androidx_test_core_ktx = "androidx.test:core-ktx:${Versions.ANDROIDX_TEST_CORE_KTX}"
    const val androidx_test_espresso_core = "androidx.test.espresso:espresso-core:${Versions.ANDROIDX_TEST_ESPRESSO}"
    const val androidx_test_espresso_contrib = "androidx.test.espresso:espresso-contrib:${Versions.ANDROIDX_TEST_ESPRESSO}"
    const val androidx_test_espresso_intents = "androidx.test.espresso:espresso-intents:${Versions.ANDROIDX_TEST_ESPRESSO}"
    const val androidx_test_runner = "androidx.test:runner:${Versions.ANDROIDX_TEST_RUNNER}"
    const val androidx_test_rules = "androidx.test:rules:${Versions.ANDROIDX_TEST_RULES}"
    const val androidx_test_uiautomator = "androidx.test.uiautomator:uiautomator:${Versions.ANDROIDX_TEST_UIAUTOMATOR}"
    const val androidx_room_testing = "androidx.room:room-testing:${Versions.ANDROIDX_ROOM}"
    const val androidx_work_testing = "androidx.work:work-testing:${Versions.ANDROIDX_WORK}"
    const val hilt_android_testing = "com.google.dagger:hilt-android-testing:${Versions.DAGGER}"
    const val espresso_core = "androidx.test.espresso:espresso-core:${Versions.ESPRESSO}"
    const val espresso_contrib = "androidx.test.espresso:espresso-contrib:${Versions.ESPRESSO}"
    const val three_ten_abp = "org.threeten:threetenbp:${Versions.THREE_TEN_ABP}"
    const val compose_ui_testing = "androidx.compose.ui:ui-test-junit4:${Versions.COMPOSE}"
    const val compose_ui_testing_manifest = "androidx.compose.ui:ui-test-manifest:${Versions.COMPOSE}"
}