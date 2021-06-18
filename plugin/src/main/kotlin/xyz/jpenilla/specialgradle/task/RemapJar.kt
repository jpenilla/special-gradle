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

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.property
import xyz.jpenilla.specialgradle.Constants
import xyz.jpenilla.specialgradle.util.taskCacheFile

/**
 * Task using SpecialSource to remap a jar file containing compiled classes using the provided mappings file.
 */
public abstract class RemapJar : Jar() {
  /**
   * Elements to add to the classpath for remapping. If there are any files in this collection,
   * the `--live` flag will be automatically passed to SpecialSource.
   */
  @get:InputFiles
  public abstract val remapClasspath: ConfigurableFileCollection

  @get:InputFile
  public abstract val inputJar: RegularFileProperty

  @get:InputFile
  public abstract val mappingsFile: RegularFileProperty

  /**
   * Whether to pass the `--reverse` flag to SpecialSource.
   */
  @get:Input
  public val reverse: Property<Boolean> = this.project.objects.property<Boolean>().convention(false)

  /**
   * Whether to silence log output from SpecialSource.
   */
  @get:Internal
  public val quiet: Property<Boolean> = this.project.objects.property<Boolean>().convention(false)

  @TaskAction
  override fun copy() {
    val specialSourceConfig = this.project.configurations.named(Constants.SPECIAL_SOURCE_CONFIG).get()
    val specialSourceJar = specialSourceConfig.asFileTree.singleOrNull()
      ?: error("Resolved too many files in the SpecialSource configuration!")
    val inputJar = this@RemapJar.inputJar.get().asFile
    val mappingsFile = this@RemapJar.mappingsFile.get().asFile
    val dest = this.taskCacheFile("jar")

    this.project.javaexec {
      this.mainClass.set("net.md_5.specialsource.SpecialSource")
      this.classpath(specialSourceJar)
      this.args(
        "-m",
        mappingsFile.absolutePath,
        "-i",
        inputJar.absolutePath,
        "-o",
        dest.absolutePath
      )
      if (!this@RemapJar.remapClasspath.isEmpty) {
        this.args("--live")
        this.classpath(this@RemapJar.remapClasspath)
      }
      if (this@RemapJar.quiet.get()) {
        this.args("--quiet")
      }
      if (this@RemapJar.reverse.get()) {
        this.args("--reverse")
      }
    }.assertNormalExitValue()

    this.from(this.project.zipTree(dest))
    super.copy()
  }
}
