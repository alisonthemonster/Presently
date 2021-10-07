object Versions {
    const val COMPILE_SDK = 31
    const val TARGET_SDK = 30
    const val MIN_SDK = 21

    const val KOTLIN = "1.4.32" //https://developer.android.com/jetpack/androidx/releases/core
    const val ANDROIDX_CORE_KTX = "1.6.0" //https://developer.android.com/jetpack/androidx/releases/core
    const val ANDROIDX_COMPAT = "1.3.0"
    const val ANDROIDX_CONSTRAINT = "2.0.4"
    const val ANDROIDX_LIFECYCLE_EXT = "2.2.0"
    const val ANDROIDX_LIFECYCLE = "2.3.1"
    const val ANDROIDX_RECYCLER_VIEW = "1.2.0"
    const val FRAGMENT_TESTING = "1.3.4"
    const val ANDROIDX_TEST_JUNIT = "1.1.2"
    const val ANDROIDX_TEST_CORE_KTX = "1.3.0"
    const val ANDROIDX_TEST_ESPRESSO = "3.3.0"
    const val ANDROIDX_TEST_RULES = "1.3.0"
    const val FIREBASE = "28.2.1" //https://firebase.google.com/support/release-notes/android
    const val DAGGER = "2.38.1" //https://github.com/google/dagger/releases
    const val ASSISTED_INJECT = "0.3.2" //TODO use dagger's version: https://dagger.dev/dev-guide/assisted-injection.html
    const val HILT_ANDROID = "1.0.0-alpha03" //https://developer.android.com/jetpack/androidx/releases/hilt
    const val JUNIT = "4.13.2"
    const val MOCKITO_KOTLIN = "2.0.0"
    const val MOCKITO_ANDROID = "2.23.0"
    const val ROBOLECTRIC = "4.6.1" //https://github.com/robolectric/robolectric/releases/
    const val TRUTH = "1.1.3"
    const val MAVERICKS = "2.2.0" //https://github.com/airbnb/mavericks/releases
}

object Libraries {
   const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}"
   const val androidx_core_ktx = "androidx.core:core-ktx:${Versions.ANDROIDX_CORE_KTX}"
   const val firebase_bom = "com.google.firebase:firebase-bom:${Versions.FIREBASE}"
   const val firebase_analytics_ktx = "com.google.firebase:firebase-analytics-ktx"
   const val firebase_crashlytics = "com.google.firebase:firebase-crashlytics"
   const val dagger = "com.google.dagger:dagger:${Versions.DAGGER}"
   const val dagger_compiler = "com.google.dagger:dagger-compiler:${Versions.DAGGER}"
   const val dagger_android_support = "com.google.dagger:dagger-android-support:${Versions.DAGGER}"
   const val dagger_android_processor = "com.google.dagger:dagger-android-processor:${Versions.DAGGER}"
   const val hilt = "com.google.dagger:hilt-android:${Versions.DAGGER}"
   const val hilt_compiler = "com.google.dagger:hilt-android-compiler:${Versions.DAGGER}"
   const val hilt_viewmodel = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.HILT_ANDROID}"
   const val hilt_android_compiler = "androidx.hilt:hilt-compiler:${Versions.HILT_ANDROID}"
   const val assisted_inject_annotations = "com.squareup.inject:assisted-inject-annotations-dagger2:${Versions.ASSISTED_INJECT}"
   const val assisted_inject_processor = "com.squareup.inject:assisted-inject-processor-dagger2:${Versions.ASSISTED_INJECT}"
   const val mavericks = "com.airbnb.android:mavericks:${Versions.MAVERICKS}"
   const val mavericks_mocking = "com.airbnb.android:mavericks-mocking:${Versions.MAVERICKS}"
   const val androidx_compat = "androidx.appcompat:appcompat:${Versions.ANDROIDX_COMPAT}"
   const val androidx_constraint_layout = "androidx.constraintlayout:constraintlayout:${Versions.ANDROIDX_CONSTRAINT}"
   const val androidx_lifecycle_ext = "androidx.lifecycle:lifecycle-extensions:${Versions.ANDROIDX_LIFECYCLE_EXT}"
   const val androidx_viewmodel_ktx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.ANDROIDX_LIFECYCLE}"
   const val androidx_lifecycle_runtime_ktx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.ANDROIDX_LIFECYCLE}"
   const val androidx_recycler_view = "androidx.recyclerview:recyclerview:${Versions.ANDROIDX_RECYCLER_VIEW}"
}

object TestLibraries {
    const val junit = "junit:junit:${Versions.JUNIT}"
    const val mockito_kotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.MOCKITO_KOTLIN}"
    const val mockito_android = "org.mockito:mockito-android::${Versions.MOCKITO_ANDROID}"
    const val robolectric = "org.robolectric:robolectric:${Versions.ROBOLECTRIC}"
    const val truth = "com.google.truth:truth:${Versions.TRUTH}"
    const val fragment_testing = "androidx.fragment:fragment-testing:${Versions.FRAGMENT_TESTING}"
    const val mavericks_testing = "com.airbnb.android:mavericks-testing:${Versions.MAVERICKS}"
    const val androidx_test_junit = "androidx.test.ext:junit:${Versions.ANDROIDX_TEST_JUNIT}"
    const val androidx_test_core_ktx = "androidx.test:core-ktx:${Versions.ANDROIDX_TEST_CORE_KTX}"
    const val androidx_test_espresso_core = "androidx.test.espresso:espresso-core:${Versions.ANDROIDX_TEST_ESPRESSO}"
    const val androidx_test_espresso_contrib = "androidx.test.espresso:espresso-contrib:${Versions.ANDROIDX_TEST_ESPRESSO}"
    const val androidx_test_espresso_intents = "androidx.test.espresso:espresso-intents:${Versions.ANDROIDX_TEST_ESPRESSO}"
    const val androidx_test_rules = "androidx.test.rules:${Versions.ANDROIDX_TEST_RULES}"
    const val hilt_android_testing = "hilt-android-testing::${Versions.DAGGER}"
}