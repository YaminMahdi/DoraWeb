plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.dora.web"
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        applicationId = ProjectConfig.applicationId
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
        versionCode = ProjectConfig.versionCode
        versionName = ProjectConfig.versionName

        buildConfigField("String", "BASE_URL", "\"${ProjectConfig.BASE_URL}\"")
        buildConfigField("boolean", "isDarkMode", "Boolean.parseBoolean(\"${ProjectConfig.isDarkMode}\")")
        buildConfigField("boolean", "showToolbar", "Boolean.parseBoolean(\"${ProjectConfig.showToolbar}\")")
        buildConfigField("boolean", "showLoading", "Boolean.parseBoolean(\"${ProjectConfig.showLoading}\")")
        buildConfigField("boolean", "showMenu", "Boolean.parseBoolean(\"${ProjectConfig.showMenu}\")")
        resValue("string", "app_name", ProjectConfig.applicationName)
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
        jvmToolchain(ProjectConfig.javaVersion.toString().toInt())
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