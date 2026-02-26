pluginManagement {
    repositories {
        google()       // This must be first!
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()       // This is for your app's libraries
        mavenCentral()
    }
}

rootProject.name = "AutoDishes"
include(":app")