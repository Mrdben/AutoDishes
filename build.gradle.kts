plugins {
    // Android Gradle Plugin (AGP)
    id("com.android.application") version "8.2.2" apply false

    // Kotlin Android Plugin - Synchronized to 1.9.10
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false

    // KSP Plugin - Must match Kotlin version (1.9.10)
    // Format: [KotlinVersion]-[KSPRelease]
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}