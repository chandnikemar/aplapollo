        plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.apolloapl"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.apolloapl"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
            vectorDrawables.useSupportLibrary = true

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
        dataBinding = true
        viewBinding = true

    }
}
        dependencies {

            // ================= LOCAL LIBS =================

            implementation(files("libs/ZSDK_ANDROID_BTLE.jar"))
            implementation(files("libs/ZSDK_ANDROID_API.jar"))
            implementation(files("libs/API3_LIB-release-2.0.2.110.aar"))

            // ================= CORE =================

            implementation("androidx.core:core-ktx:1.13.1")

            implementation("androidx.appcompat:appcompat:1.7.0")

            implementation("com.google.android.material:material:1.12.0")

            implementation("androidx.constraintlayout:constraintlayout:2.2.1")

            implementation("androidx.cardview:cardview:1.0.0")

            implementation("androidx.legacy:legacy-support-v4:1.0.0")

            // ================= LIFECYCLE =================

            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

            implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")

            // ================= COROUTINES =================

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

            // ================= NAVIGATION =================

            val nav_version = "2.7.7"

            implementation(
                "androidx.navigation:navigation-fragment-ktx:$nav_version"
            )

            implementation(
                "androidx.navigation:navigation-ui-ktx:$nav_version"
            )

            androidTestImplementation(
                "androidx.navigation:navigation-testing:$nav_version"
            )

            // ================= RETROFIT =================

            implementation("com.squareup.retrofit2:retrofit:2.11.0")

            implementation("com.squareup.retrofit2:converter-gson:2.11.0")

            implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

            // ================= FIREBASE =================

            implementation("com.google.firebase:firebase-auth:23.0.0")

            // ================= MAPS =================

            implementation("com.google.android.gms:play-services-maps:19.1.0")

            // ================= BARCODE =================

            implementation("me.dm7.barcodescanner:zxing:1.9")

            implementation("com.google.zxing:core:3.5.3")

            implementation("com.journeyapps:zxing-android-embedded:4.3.0")

            // ================= IMAGE =================

            implementation("com.github.bumptech.glide:glide:4.16.0")

            // ================= ZEBRA =================

            implementation("com.symbol:emdk:9.1.1")

            // ================= TOAST =================

            implementation("com.github.GrenderG:Toasty:1.5.2")

            // ================= TEST =================

            testImplementation("junit:junit:4.13.2")

            androidTestImplementation(
                "androidx.test.ext:junit:1.2.1"
            )

            androidTestImplementation(
                "androidx.test.espresso:espresso-core:3.6.1"
            )
        }
