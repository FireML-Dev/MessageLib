rootProject.name = "MessageLib"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // compileOnly dependencies
            library("paper-api", "io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
            library("placeholderapi", "me.clip:placeholderapi:2.11.6")

            // implementation dependencies

            // paperLibrary dependencies

            // Gradle plugins
        }
    }
}
