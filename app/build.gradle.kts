plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 namespace = "com.olegovichhh.audioconverter"; compileSdk = 35
 defaultConfig { applicationId = "com.olegovichhh.audioconverter"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0" }
 externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt"); version = "3.22.1" } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
 packaging { jniLibs { pickFirsts += setOf("lib/*/libc++_shared.so") } }
}
dependencies {
 implementation("androidx.core:core-ktx:1.15.0")
 implementation("androidx.appcompat:appcompat:1.7.0")
 implementation("com.google.android.material:material:1.12.0")
 implementation("dev.ffmpegkit-maintained:ffmpeg-kit-audio:8.1.7")
 implementation("dev.kotlinds:fluidsynth-kmp:1.1.0")
}