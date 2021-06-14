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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.property
import xyz.jpenilla.specialgradle.Constants

public abstract class RemapJar : Jar() {
  @get:InputFile
  public abstract val inputJar: RegularFileProperty

  @get:InputFile
  public abstract val mappingsFile: RegularFileProperty

  @get:InputFile
  public abstract val remapDependency: RegularFileProperty

  @get:Input
  public val reverse: Property<Boolean> = this.project.objects.property<Boolean>().convention(false)

  @TaskAction
  override fun copy() {
    val specialSourceConfig = this.project.configurations.named(Constants.SPECIAL_SOURCE_CONFIGURATION_NAME).get()
    val specialSourceJar = specialSourceConfig.asFileTree.singleOrNull() ?: error("Resolved too many files in the SpecialSource configuration!")
    val dir = this@RemapJar.project.layout.buildDirectory.dir("tmp/special-gradle").get()
    dir.asFile.mkdirs()
    val inputJar = this@RemapJar.inputJar.get().asFile
    val mappingsFile = this@RemapJar.mappingsFile.get().asFile
    val dependency = this@RemapJar.remapDependency.get().asFile
    val dest = dir.file("${inputJar.nameWithoutExtension}-mapped-${mappingsFile.nameWithoutExtension}.jar").asFile
    this.project.javaexec {
      this.main = "net.md_5.specialsource.SpecialSource"
      this.classpath(specialSourceJar, dependency)
      this.args(
        "-i",
        inputJar.absolutePath,
        "-o",
        dest.absolutePath,
        "-m",
        mappingsFile.absolutePath,
        "--live",
        "--quiet"
      )
      if (this@RemapJar.reverse.get()) {
        this.args("--reverse")
      }
    }
    this.from(this.project.zipTree(dest))
    super.copy()
  }
}
