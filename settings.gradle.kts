pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // <-- Add this line
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Smart Fit"
include(":app")
