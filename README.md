## MessageLib

A library to easily handle modern messages in Paper plugins.

### Basic Usage
The basic classes for this library are [ComponentSingleMessage](https://github.com/FireML-Dev/MessageLib/blob/main/src/main/java/uk/firedev/messagelib/message/ComponentSingleMessage.java) and [ComponentListMessage](https://github.com/FireML-Dev/MessageLib/blob/main/src/main/java/uk/firedev/messagelib/message/ComponentListMessage.java).

These classes are immutable, just like Adventure's Component class. All methods should be self-explanatory.

### Gradle (Kotlin)
To use this library, you need to shade it into your plugin using Gradle and the Shadow plugin.

```kotlin
repositories {
    maven("https://repo.codemc.io/repository/FireML/")
}

dependencies {
    compileOnly("uk.firedev:MessageLib:1.0.8") // This may not be the latest version.
}
```
