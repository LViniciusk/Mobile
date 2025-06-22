
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.hilt.android) apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.12" apply false
}