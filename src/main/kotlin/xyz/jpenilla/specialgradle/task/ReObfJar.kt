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
package xyz.jpenilla.specialgradle.task

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import xyz.jpenilla.specialgradle.Constants

public abstract class ReObfJar : Jar() {
  @get:InputFile
  public abstract val inputJar: RegularFileProperty

  @TaskAction
  override fun copy() {
    val mappingsFile = this.project.configurations.named(Constants.REOBF_MAPPINGS_CONFIGURATION_NAME).get().asFileTree
      .singleOrNull() ?: error("Resolved too many files in the mappings configuration!")
    val specialSourceConfig = this.project.configurations.named(Constants.SPECIAL_SOURCE_CONFIGURATION_NAME).get()
    val specialSourceJar = specialSourceConfig.asFileTree.singleOrNull() ?: error("Resolved too many files in the SpecialSource configuration!")
    val dir = this@ReObfJar.project.layout.buildDirectory.dir("special-gradle").get()
    dir.asFile.mkdirs()
    val dest = dir.file("reobf.jar").asFile
    dest.delete()
    this.project.javaexec {
      this.classpath(specialSourceJar)
      this.args(
        "-i",
        this@ReObfJar.inputJar.get().asFile.absolutePath,
        "-o",
        dest.absolutePath,
        "-m",
        mappingsFile.absolutePath,
        "--reverse"
      )
    }
    this.from(this.project.zipTree(dest))
    super.copy()
  }
}
