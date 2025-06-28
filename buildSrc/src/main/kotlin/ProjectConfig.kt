import org.gradle.api.JavaVersion

@Suppress("ConstPropertyName")
object ProjectConfig {
    const val applicationName = "Dora Web"
    const val applicationId = "com.dora.user"
    const val minSdk = 21
    const val compileSdk = 36
    const val targetSdk = 36
    const val versionCode = 1
    const val versionName = "1.0.0"

    val javaVersion = JavaVersion.VERSION_21
    const val BASE_URL = "https://www.nicdostudio.com"

    const val isDarkMode = false
    const val showToolbar = true
    const val showLoading = true
    const val showMenu = true
}