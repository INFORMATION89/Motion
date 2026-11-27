pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Not "Motion": a root named like its :motion project clashes in apps that include this build
// (Gradle generates the same `projects.motion` accessor for both).
rootProject.name = "MotionFramework"
include(":motion", ":motion-ui", ":motion-lab")
