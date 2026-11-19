import com.android.build.api.dsl.LibraryExtension

// Standalone motion framework (Apache-2.0). Depends only on Compose: no Breezy or Solstice code.
plugins {
    id("breezy.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

configure<LibraryExtension> {
    namespace = "io.github.information89.motion"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    api(libs.compose.animation)
    implementation(libs.compose.foundation)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.junit.platform)
}
