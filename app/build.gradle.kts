plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.guarena"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.guarena"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // ✅ PACKAGING BLOCK TO FIX EMAIL DEPENDENCY CONFLICTS
    packaging {
        resources {
            excludes += listOf("META-INF/NOTICE.md", "META-INF/LICENSE.md")
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    implementation(libs.work.runtime)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.core)
    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ✅ EMAIL FUNCTIONALITY (Fixed with packaging block above)
    implementation("com.sun.mail:android-mail:1.6.6")
    implementation("com.sun.mail:android-activation:1.6.7")

    // ✅ ADDITIONAL DEPENDENCIES FOR COMPLETE FUNCTIONALITY
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.fragment:fragment:1.6.2")
    implementation ("androidx.work:work-runtime:2.8.1")
}


//plugins {
//    alias(libs.plugins.android.application)
//}
//
//android {
//    namespace = "com.example.guarena"
//    compileSdk = 34
//
//    defaultConfig {
//        applicationId = "com.example.guarena"
//        minSdk = 24
//        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//
//    packaging {
//        resources {
//            excludes += ("META-INF/NOTICE.md', 'META-INF/LICENSE.md")
//        }
//    }
//}
//
//dependencies {
//
//    implementation(libs.appcompat)
//    implementation(libs.material)
//    implementation(libs.activity)
//    implementation(libs.constraintlayout)
//    implementation(libs.swiperefreshlayout)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.ext.junit)
//    androidTestImplementation(libs.espresso.core)
//    implementation("com.github.bumptech.glide:glide:4.16.0")
//    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
//    implementation("com.sun.mail:android-mail:1.6.6")
//    implementation("com.sun.mail:android-activation:1.6.7")
//}