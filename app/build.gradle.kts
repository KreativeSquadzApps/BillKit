import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.org.jetbrains.kotlin.kapt)
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.navigation.safeargs.kotlin)


}

android {
    namespace = "com.kreativesquadz.billkit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kreativesquadz.billkit"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true

    }
//    dataBinding {
//        enabled = true
//    }



}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.play.services.mlkit.barcode.scanning)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.annotation)
    ksp(libs.androidx.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.hilt.android)
    implementation(libs.timber)
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.converter.gson)
    implementation(libs.neumorphism)
    implementation (libs.kotlin.reflect)
    kapt(libs.androidx.databinding.compiler)
    implementation(libs.work.runtime.ktx)
    implementation (libs.androidx.hilt.work)
    implementation (libs.androidx.startup.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.hilt.navigation.fragment)
    implementation (libs.androidx.camera.camera2)
    implementation (libs.androidx.camera.lifecycle)
    implementation (libs.androidx.camera.view)
    implementation(libs.logging.interceptor)
    implementation(libs.itext7.core)

}
