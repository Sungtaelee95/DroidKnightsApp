plugins {
    alias(libs.plugins.droidknights.kotlin.library)
    id("kotlinx-serialization")
}

dependencies {
    api(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
}
