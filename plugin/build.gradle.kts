plugins {
  `kotlin-dsl`
  id("com.gradle.plugin-publish")
  id("net.kyori.indra")
  id("net.kyori.indra.license-header")
  id("net.kyori.indra.publishing.gradle-plugin")
}

group = "xyz.jpenilla"
version = "1.0.0-SNAPSHOT"
description = "todo"

repositories {
  mavenCentral()
  gradlePluginPortal()
}

kotlin {
  explicitApi()
}

tasks {
  compileKotlin {
    kotlinOptions.apiVersion = "1.4"
    kotlinOptions.jvmTarget = "1.8"
  }
}

indra {
  javaVersions {
    target(8)
  }
  apache2License()
  github("jpenilla", "special-gradle")
  publishSnapshotsTo("jmp", "https://repo.jpenilla.xyz/snapshots")
  configurePublications {
    pom {
      developers {
        developer {
          id.set("jmp")
          timezone.set("America/Los Angeles")
        }
      }
    }
  }
}

license {
  header(file("../LICENSE_HEADER"))
}

indraPluginPublishing {
  plugin(
    "special-gradle",
    "xyz.jpenilla.specialgradle.SpecialGradle",
    "Special Gradle",
    project.description,
    listOf("minecraft", "remapping", "spigot", "specialsource")
  )
  website("https://github.com/jpenilla/special-gradle")
}
