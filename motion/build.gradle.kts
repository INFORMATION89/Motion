import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Motion: a motion framework for Jetpack Compose (Apache-2.0). Depends only on Compose.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.spotless)
}

// These coordinates support Gradle composite-build consumers through dependency substitution.
group = "io.github.information89"

android {
    namespace = "io.github.information89.motion"
    compileSdk = libs.versions.sdk.compile.get().toInt()
    buildToolsVersion = libs.versions.build.tools.get()

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

spotless {
    kotlin {
        target("**/*.kt", "**/*.kts")
        targetExclude("**/build/**/*.kt")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    api(libs.compose.animation)
    implementation(libs.compose.foundation)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.junit.platform)
}
