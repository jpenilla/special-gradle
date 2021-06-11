# Special Gradle

[![build](https://img.shields.io/github/checks-status/jpenilla/special-gradle/master?label=build)](https://github.com/jpenilla/special-gradle/actions) [![license](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE)

TODO

### Usage

Add the repo for snapshots in settings

```kotlin
pluginManagement {
  repositories {
    maven("https://repo.jpenilla.xyz/snapshots/")
  }
}
```

Apply the plugin in your project buildscript.

```kotlin
plugins {
  // Apply the plugin
  id("xyz.jpenilla.special-gradle") version "1.0.0-SNAPSHOT"
}
```
