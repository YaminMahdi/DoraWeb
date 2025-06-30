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
        buildConfigField("boolean", "showVisitWebsite", "Boolean.parseBoolean(\"${ProjectConfig.showVisitWebsite}\")")
        buildConfigField("boolean", "showShareApp", "Boolean.parseBoolean(\"${ProjectConfig.showShareApp}\")")
        buildConfigField("boolean", "showWelcome", "Boolean.parseBoolean(\"${ProjectConfig.showWelcome}\")")
        resValue("string", "app_name", ProjectConfig.applicationName)
        resValue("color", "colorPrimary", ProjectConfig.colorPrimary)
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
    kotlin {
        jvmToolchain(ProjectConfig.javaVersion)
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    applicationVariants.all {
        outputs.forEach {
            val output = it as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "${ProjectConfig.applicationName}-v$versionName($versionCode)-$name.apk"
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
    implementation(libs.androidx.swiperefreshlayout)

    // Play In-App Update:
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)
}

tasks.register("updateRootProjectName") {
    group = "configuration"
    description = "Updates rootProject.name in settings.gradle.kts from ProjectConfig"

    doLast {
        val projectConfigClass = Class
            .forName("ProjectConfig")
            .kotlin
            .objectInstance ?: error("ProjectConfig not found")

        val name = projectConfigClass
            .javaClass
            .getDeclaredField("applicationName")
            .get(projectConfigClass) as String

        val settingsFile = rootProject.file("settings.gradle.kts")
        val content = settingsFile.readText()

        // Fixed regex pattern - removed the extra quote at the end
        val updatedContent = content.replace(Regex("""rootProject\.name\s*=\s*"[^"]*"""")) {
            "rootProject.name = \"$name\""
        }

        settingsFile.writeText(updatedContent)
        println("rootProject.name updated to \"$name\" in settings.gradle.kts")
    }
}

tasks.named("preBuild") {
    dependsOn("updateRootProjectName")
}