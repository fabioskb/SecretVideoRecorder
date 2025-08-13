import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.fabiosf34.secretvideorecorder"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fabiosf34.secretvideorecorder"
        minSdk = 24
        targetSdk = 36
        // 08/08/2025 19:53
        versionCode = 16
        versionName = "1.0.0-beta.14"
        ///////////

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //noinspection WrongGradleMethod
        ksp {
            arg("room.schemaLocation", "${projectDir}/schemas")
        }

        // Obter o AdMob ID's do local.properties
        // Fornecer um valor padrão ou de teste se não encontrado é uma boa prática,
        // especialmente para que outros desenvolvedores ou o CI possam construir o projeto.
        val admobAppId = localProperties.getProperty("ADMOB_APP_ID")
            ?: "ca-app-pub-3940256099942544~3347511713" // ID de teste do AdMob
        val admobBannerId = localProperties.getProperty("ADMOB_BANNER_ID")
            ?: "ca-app-pub-3940256099942544/6300978111" // ID de teste do AdMob (banner)
        val admobInterstitialId = localProperties.getProperty("ADMOB_INTERSTITIAL_ID")
            ?: "ca-app-pub-3940256099942544/1033173712"

        // Disponibiliza o AdMob ID para o Manifest
        // (e também para Buildconfig, se você quiser acessa-lo via código também)
        manifestPlaceholders["admobAppId"] = admobAppId
        buildConfigField("String", "ADMOB_APP_ID", "\"$admobAppId\"")
        manifestPlaceholders["admobBannerId"] = admobBannerId
        buildConfigField("String", "ADMOB_BANNER_ID", "\"$admobBannerId\"")
        manifestPlaceholders["admobInterstitialId"] = admobInterstitialId
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$admobInterstitialId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
//            room {
//                schemaDirectory(
//                    project.file(
//                        "schemas"
//                    ).absolutePath
//                )
//            }
        }
        debug {
            applicationIdSuffix = ".debug"
        }
//        debug {
//            room {
//                schemaDirectory(
//                    project.file(
//                        "schemas"
//                    ).absolutePath
//                )
//            }
//        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
//        jvmToolchain(11)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    lint {
        abortOnError = false
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {


    // Google ADS
    implementation(libs.play.services.ads) {
        exclude(group = "com.google.guava", module = "listenablefuture")
        exclude(group = "com.google.guava", module = "guava") // Tente excluir o Guava também
    }

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Room
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)

//    // opcional - Suporte a extensões Kotlin e Coroutines para Room
//    implementation("androidx.room:room-ktx:$roomVersion")
//    // opcional - Suporte a RxJava2 para Room
//    implementation("androidx.room:room-rxjava2:$roomVersion")
//    // opcional - Suporte a RxJava3 para Room
//    implementation("androidx.room:room-rxjava3:$roomVersion")
//    // opcional - Suporte a Guava para Room, incluindo Optional e ListenableFuture
//    implementation("androidx.room:room-guava:$roomVersion")
//    // opcional - Integração com Paging 3
//    implementation("androidx.room:room-paging:$roomVersion")

    // ... Uso do listenableFuture no projeto
    implementation(libs.androidx.concurrent.futures.ktx)
    // CameraX
    implementation(libs.androidx.camera.core) {
        exclude(group = "com.google.guava", module = "listenablefuture")
         exclude(group = "com.google.guava", module = "guava") // Opcional, teste excluir o Guava também
    }
    implementation(libs.androidx.camera.camera2) {
        exclude(group = "com.google.guava", module = "listenablefuture")
         exclude(group = "com.google.guava", module = "guava") // Opcional, teste excluir o Guava também
    }
    implementation(libs.camera.lifecycle) {
        exclude(group = "com.google.guava", module = "listenablefuture")
         exclude(group = "com.google.guava", module = "guava") // Opcional, teste excluir o Guava também
    }
    implementation(libs.camera.view) {
        exclude(group = "com.google.guava", module = "listenablefuture")
         exclude(group = "com.google.guava", module = "guava") // Opcional, teste excluir o Guava também
    }
    implementation(libs.androidx.camera.extensions) {
        exclude(group = "com.google.guava", module = "listenablefuture")
         exclude(group = "com.google.guava", module = "guava") // Opcional, teste excluir o Guava também
    }
//    implementation(libs.camera.core)
//    implementation(libs.camera.camera2)
//    implementation(libs.camera.lifecycle)
//    implementation(libs.camera.view)

    // Biometric
    implementation(libs.androidx.biometric.ktx)

    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.legacy.support.v4) {
        exclude(group = "com.google.guava", module = "listenablefuture")
        exclude(group = "com.google.guava", module = "guava") // Opcional, teste excluir o Guava também
    }
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}