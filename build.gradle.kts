plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.0" apply false
}