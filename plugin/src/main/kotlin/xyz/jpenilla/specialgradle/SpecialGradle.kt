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
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.repositories
import xyz.jpenilla.specialgradle.task.RemapJar

public abstract class SpecialGradle : Plugin<Project> {
  override fun apply(target: Project) {
    val specialGradleExtension = target.extensions.create<SpecialGradleExtension>(Constants.SPECIAL_GRADLE_EXTENSION_NAME)
    target.configurations.register(Constants.SPECIAL_SOURCE_CONFIGURATION_NAME) {
      this.isTransitive = false
    }
    target.configurations.register(Constants.MOJANG_TO_OBF_MAPPINGS_CONFIGURATION_NAME)
    target.configurations.register(Constants.OBF_TO_RUNTIME_MAPPINGS_CONFIGURATION_NAME)
    target.configurations.register(Constants.REMAPPED_DEPENDENCIES_MOJANG_CONFIGURATION_NAME)
    target.configurations.register(Constants.REMAPPED_DEPENDENCIES_OBF_CONFIGURATION_NAME)

    val obf = target.tasks.register<RemapJar>(Constants.OBF_JAR_TASK_NAME) {
      this.reverse.set(true)
      this.archiveClassifier.set("obf")
      this.mappingsFile.set(this.project.layout.file(this.project.configurations.named(Constants.MOJANG_TO_OBF_MAPPINGS_CONFIGURATION_NAME).map {
        it.singleFile
      }))
      this.remapDependency.set(this.project.layout.file(this.project.configurations.named(Constants.REMAPPED_DEPENDENCIES_MOJANG_CONFIGURATION_NAME).map {
        it.singleFile
      }))
    }
    target.tasks.register<RemapJar>(Constants.PRODUCTION_MAPPED_JAR_TASK_NAME) {
      this.inputJar.set(obf.flatMap { it.archiveFile })
      this.archiveClassifier.set(null as String?)
      this.mappingsFile.set(this.project.layout.file(this.project.configurations.named(Constants.OBF_TO_RUNTIME_MAPPINGS_CONFIGURATION_NAME).map {
        it.singleFile
      }))
      this.remapDependency.set(this.project.layout.file(this.project.configurations.named(Constants.REMAPPED_DEPENDENCIES_OBF_CONFIGURATION_NAME).map {
        it.singleFile
      }))
    }

    target.afterEvaluate {
      this.tasks.named<AbstractArchiveTask>("jar") {
        this.archiveClassifier.set("dev")
      }
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
      if (specialGradleExtension.injectRepositories.get()) {
        this.repositories {
          this.mavenCentral()

          this.mavenLocal {
            this.metadataSources {
              this.mavenPom()
              this.artifact()
            }
            this.mavenContent {
              this.snapshotsOnly()
              this.includeGroup("org.spigotmc")
            }
          }
        }
      }
      this.dependencies {
        Constants.SPECIAL_SOURCE_CONFIGURATION_NAME(
          group = "net.md-5",
          name = "SpecialSource",
          version = specialGradleExtension.specialSourceVersion.get(),
          classifier = "shaded"
        )
      }
    }
  }
}
