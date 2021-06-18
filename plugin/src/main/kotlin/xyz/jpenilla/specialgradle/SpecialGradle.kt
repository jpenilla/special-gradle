/*
 * Special Gradle Gradle Plugin
 * Copyright (c) 2021 Jason Penilla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.jpenilla.specialgradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.properties
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.repositories
import xyz.jpenilla.specialgradle.task.BuildTools
import xyz.jpenilla.specialgradle.task.BuildToolsVersion
import xyz.jpenilla.specialgradle.task.DownloadFile
import xyz.jpenilla.specialgradle.task.RemapJar
import xyz.jpenilla.specialgradle.util.cache
import xyz.jpenilla.specialgradle.util.sharedCache
import java.net.URL
import java.util.Calendar

public abstract class SpecialGradle : Plugin<Project> {
  override fun apply(target: Project) {
    val specialGradleExtension = target.extensions.create<SpecialGradleExtension>(Constants.SPECIAL_GRADLE_EXTENSION_NAME)
    target.configurations.register(Constants.SPECIAL_SOURCE_CONFIG) {
      this.isTransitive = false
    }
    val mojangMappedConfig = target.configurations.register(Constants.MOJANG_MAPPED_SERVER_CONFIG)

    target.tasks.register<Delete>(Constants.CLEAN_SPECIAL_GRADLE_CACHE_TASK_NAME) {
      this.group = Constants.TASK_GROUP
      this.description = "Clean Special Gradle's shared and project caches."
      this.delete(this.project.layout.cache(Constants.SPECIAL_GRADLE_PATH))
      this.delete(this.project.sharedCache(Constants.SPECIAL_GRADLE_PATH))
    }

    val cal = Calendar.getInstance()
    val today = "today" to "${cal.get(Calendar.DAY_OF_MONTH)} ${cal.get(Calendar.MONTH)}"

    val findBuildToolsVersion = target.tasks.register<BuildToolsVersion>(Constants.FETCH_BUILD_TOOLS_VERSION_TASK_NAME) {
      this.group = Constants.TASK_GROUP
      this.description = "Fetches the number of the latest build of BuildTools on Spigot's Jenkins."
      this.inputs.properties(today)
    }

    val downloadBuildTools = target.tasks.register<DownloadFile>(Constants.DOWNLOAD_BUILD_TOOLS_TASK_NAME) {
      this.group = Constants.TASK_GROUP
      this.description = "Downloads BuildTools from Spigot's Jenkins server."
      this.remoteURL.set(findBuildToolsVersion.flatMap { it.version }.map {
        URL(Constants.BUILD_TOOLS_DOWNLOAD_LINK.replace("{}", it.asFile.readText()))
      })
      this.destination.set(this.project.sharedCache(Constants.BUILD_TOOLS_JAR_PATH))
    }

    val buildTools = target.tasks.register<BuildTools>(Constants.BUILD_TOOLS_TASK_NAME) {
      this.group = Constants.TASK_GROUP
      this.description = "Runs BuildTools to acquire mappings and dependencies."
      this.buildToolsJar.set(downloadBuildTools.flatMap { it.destination })
      this.minecraftVersion.set(specialGradleExtension.minecraftVersion)
      this.inputs.properties(today)
    }

    // Mojang -> OBF
    val obf = target.tasks.register<RemapJar>(Constants.OBF_JAR_TASK_NAME) {
      this.group = Constants.TASK_GROUP
      this.reverse.set(true)
      this.archiveClassifier.set("obf")
      this.mappingsFile.set(buildTools.flatMap { it.mojangMappingsTxt })
      this.remapClasspath.from(buildTools.flatMap { it.mojangMappedServer })
    }

    // OBF -> Spigot
    target.tasks.register<RemapJar>(Constants.PRODUCTION_MAPPED_JAR_TASK_NAME) {
      this.group = Constants.TASK_GROUP
      this.inputJar.set(obf.flatMap { it.archiveFile })
      this.archiveClassifier.set(null as String?)
      this.mappingsFile.set(buildTools.flatMap { it.spigotMappingsCsrg })
      this.remapClasspath.from(buildTools.flatMap { it.obfServer })
    }

    target.afterEvaluate {
      target.configurations.named(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME) {
        this.extendsFrom(mojangMappedConfig.get())
      }

      this.tasks.named<AbstractArchiveTask>("jar") {
        this.archiveClassifier.set("dev")
      }

      // Configure input jar from shadowJar/jar if not already configured
      if (!obf.get().inputJar.isPresent) {
        obf.configure {
          val shadowJar = tasks.findByName("shadowJar") as AbstractArchiveTask?
          if (shadowJar != null) {
            this.inputJar.set(shadowJar.archiveFile)
          } else {
            this.inputJar.set(this@afterEvaluate.tasks.named<AbstractArchiveTask>("jar").flatMap { it.archiveFile })
          }
        }
      }

      // Inject mavenCentral repo for SpecialSource
      if (specialGradleExtension.injectRepositories.get()) {
        this.repositories {
          this.mavenCentral()
        }
      }

      this.dependencies {
        // Add SpecialSource dependency
        Constants.SPECIAL_SOURCE_CONFIG(
          group = "net.md-5",
          name = "SpecialSource",
          version = specialGradleExtension.specialSourceVersion.get(),
          classifier = "shaded"
        )

        // Add dependency on Mojang-mapped Spigot
        if (specialGradleExtension.injectSpigotDependency.get()) {
          Constants.MOJANG_MAPPED_SERVER_CONFIG(this@afterEvaluate.files(buildTools.flatMap { it.mojangMappedServer }))
        }
      }
    }
  }
}
