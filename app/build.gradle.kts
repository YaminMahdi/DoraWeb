plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.dora.web"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dora.web"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
//            isDebuggable = false
//            isShrinkResources = true
//            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    kotlin{
        jvmToolchain(21)
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    applicationVariants.all {
        outputs.forEach {
            val output = it as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "${rootProject.name}-v$versionName($versionCode)-$name.apk"
        }
    }
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
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core.splashscreen)

    // Play In-App Update:
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)
}