plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.alexaventuras.bienvenida"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.alexaventuras.bienvenida"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"
    }

    // Siempre la misma firma: así cada versión nueva se instala encima de la anterior y la tele conserva su
    // configuración. Es una app que se instala directo (no Play Store); la firma solo sirve para eso.
    signingConfigs {
        create("bienvenida") {
            storeFile = file("firma-bienvenida.jks")
            storePassword = "bienvenida-tv"
            keyAlias = "bienvenida"
            keyPassword = "bienvenida-tv"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("bienvenida")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
