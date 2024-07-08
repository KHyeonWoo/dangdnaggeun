import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    //gpt통신위한 plugin
    kotlin("plugin.serialization") version "2.0.0"
}

android {
    namespace = "com.khw.computervision"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.khw.computervision"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "OPENAI_API_KEY", getApiKey())

        val keystoreFile = project.rootProject.file("local.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())

        val naverClientId = properties.getProperty("naverClientId")

        buildConfigField(
            type = "String",
            name = "naverClientId",
            value = naverClientId
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

//chatgptAPI키 관리
fun getApiKey(): String {
    val properties = Properties()
    val propFile = rootProject.file("local.properties")
    if (propFile.exists()) {
        properties.load(FileInputStream(propFile))
        return "\"${properties.getProperty("OPENAI_API_KEY")}\""
    } else {
        throw FileNotFoundException("local.properties file not found")
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.firebase.auth)
    implementation(libs.cronet.embedded)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.navigation.compose)
//    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.material3.android)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //현우 추가
    implementation("com.google.android.gms:play-services-auth:20.7.0")
//    implementation("com.google.android.gms:play-services-mlkit-subject-segmentation:16.0.0-beta1")
    implementation("com.github.CanHub:Android-Image-Cropper:4.0.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-gif:2.6.0")
    implementation("com.google.accompanist:accompanist-pager:0.20.1")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.20.1")
    implementation("com.squareup.retrofit2:retrofit:2.6.0")
    implementation("com.squareup.retrofit2:converter-gson:2.6.0")
    implementation("com.github.a914-gowtham:compose-ratingbar:1.3.12")
    implementation("com.github.skydoves:landscapist-glide:1.3.7")

    //navigation 종속성 추가 - kh
    implementation ("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation ("androidx.navigation:navigation-ui-ktx:2.3.5")
    // 좌표 종속성 추가 - dh
    implementation("com.google.android.gms:play-services-location:21.0.1")
    // 이게 뭔진 모르지만 일단 종속성 추가- kh
    implementation ("com.google.accompanist:accompanist-placeholder-material:0.24.13-rc")
    //  권한 설정하는 종속성 - kh
    implementation ("com.google.accompanist:accompanist-permissions:0.24.13-rc")
    // 네이버 지도 -dh
    implementation ("com.naver.maps:map-sdk:3.18.0")

    // kotlinx.serialization 종속성 추가 - kh
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // searchbar - kh
    implementation("androidx.compose.material3:material3:1.3.0-beta03")
}

