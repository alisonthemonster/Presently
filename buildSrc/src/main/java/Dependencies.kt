object Versions {
    const val COMPILE_SDK = 31
    const val TARGET_SDK = 30
    const val MIN_SDK = 21

    const val KOTLIN = "1.4.32" //https://developer.android.com/jetpack/androidx/releases/core
    const val ANDROIDX_CORE_KTX = "1.6.0" //https://developer.android.com/jetpack/androidx/releases/core
    const val FIREBASE = "28.2.1" //https://firebase.google.com/support/release-notes/android
    const val DAGGER = "2.38.1" //https://github.com/google/dagger/releases
    const val ASSISTED_INJECT = "0.3.2" //TODO use dagger's version: https://dagger.dev/dev-guide/assisted-injection.html
    const val HILT_ANDROID = "1.0.0-alpha03" //https://developer.android.com/jetpack/androidx/releases/hilt
    const val JUNIT = "4.13.2"
    const val MOCKITO_KOTLIN = "2.0.0"
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
}

object TestLibraries {
    const val junit = "junit:junit:${Versions.JUNIT}"
    const val mockito_kotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.MOCKITO_KOTLIN}"
    const val robolectric = "org.robolectric:robolectric:${Versions.ROBOLECTRIC}"
    const val truth = "com.google.truth:truth:${Versions.TRUTH}"
}