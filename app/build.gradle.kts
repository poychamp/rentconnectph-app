import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ph.rentconnect.app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "ph.rentconnect.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 10005
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProps = rootProject.file("local.properties")
        val mapsKey = if (localProps.exists()) {
            Properties().apply { localProps.inputStream().use(::load) }
                .getProperty("MAPS_API_KEY", "")
        } else ""
        manifestPlaceholders["mapsApiKey"] = mapsKey
    }

    signingConfigs {
        create("release") {
            val localProps = rootProject.file("local.properties")
            if (localProps.exists()) {
                val props = Properties().apply { localProps.inputStream().use(::load) }
                storeFile = rootProject.file(props.getProperty("RELEASE_STORE_FILE", ""))
                storePassword = props.getProperty("RELEASE_STORE_PASSWORD", "")
                keyAlias = props.getProperty("RELEASE_KEY_ALIAS", "")
                keyPassword = props.getProperty("RELEASE_KEY_PASSWORD", "")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("local") {
            dimension = "environment"
            applicationIdSuffix = ".local"
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2\"")
        }
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://mirthful-antwerp-mant2tajxy69.on-vapor.com\"")
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            buildConfigField("String", "BASE_URL", "\"https://staging.rentconnectph.com\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://rentconnectph.com\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.all { it.useJUnitPlatform() }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.coil.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.google.maps)
    implementation(libs.maps.compose)
    implementation(libs.navigation.compose)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.text.google.fonts)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit5.launcher)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.turbine)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}