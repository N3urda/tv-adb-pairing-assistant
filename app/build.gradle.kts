import java.io.File
import java.util.Properties

plugins { id("com.android.application"); kotlin("android") }

// Release credentials live outside the repository. Debug builds need none.
val signingPropertiesPath = System.getenv("TV_ADB_SIGNING_PROPERTIES")
val signingProperties = Properties().apply {
    if (signingPropertiesPath != null) {
        File(signingPropertiesPath).inputStream().use { load(it) }
    }
}
fun signingProperty(name: String): String =
    signingProperties.getProperty(name) ?: error("Missing $name in TV_ADB_SIGNING_PROPERTIES")

android {
    namespace = "com.hongguotv.adbremote"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hongguotv.adbremote"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        if (signingPropertiesPath != null) {
            create("release") {
                storeFile = File(signingProperty("storeFile"))
                storePassword = signingProperty("storePassword")
                keyAlias = signingProperty("keyAlias")
                keyPassword = signingProperty("keyPassword")
            }
        }
    }
    buildTypes {
        getByName("release") {
            if (signingPropertiesPath != null) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
