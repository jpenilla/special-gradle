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
package xyz.jpenilla.specialgradle.util

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Provider
import xyz.jpenilla.specialgradle.Constants
import xyz.jpenilla.specialgradle.SpecialGradle
import java.io.File
import java.util.stream.Collectors

internal data class CmdResult(val exitCode: Int, val message: String) {
  fun assertNormalExit(): CmdResult {
    if (this.exitCode != 0) {
      error("Command exited with exit code '${this.exitCode}'")
    }
    return this
  }
}

internal fun cmd(
  vararg command: String,
  dir: File,
  printOut: Boolean = false
): CmdResult =
  cmdImpl(*command, dir = dir, printOut = printOut)

private fun cmdImpl(
  vararg command: String,
  dir: File,
  printOut: Boolean = false,
  environment: Map<String, String> = emptyMap()
): CmdResult {
  val process = ProcessBuilder().apply {
    command(*command)
    redirectErrorStream(true)
    directory(dir)
    environment().putAll(environment)
  }.start()

  val output = process.inputStream.bufferedReader().use { reader ->
    val logger = Logging.getLogger(SpecialGradle::class.java)
    reader.lines()
      .peek {
        if (printOut) {
          logger.lifecycle(it)
        } else {
          logger.debug(it)
        }
      }
      .collect(Collectors.joining("\n"))
  }

  val exit = process.waitFor()
  return CmdResult(exit, output)
}

internal val ProjectLayout.cache: File
  get() = this.projectDirectory.dir(".gradle/${Constants.CACHE_PATH}").asFile

internal fun ProjectLayout.cache(path: String): File =
  this.cache.resolve(path)

internal val Project.sharedCache: File
  get() = this.gradle.gradleUserHomeDir.resolve(Constants.CACHE_PATH)

internal fun Project.sharedCache(path: String): File =
  this.sharedCache.resolve(path)

internal val Task.taskCacheDir: File
  get() = this.project.layout.cache("${Constants.TASK_CACHE_PATH}/${this.name}")

internal fun Task.taskCacheFile(extension: String): File =
  this.project.layout.cache("${Constants.TASK_CACHE_PATH}/${this.name}.$extension")

internal fun Project.fileProvider(file: File): Provider<RegularFile> =
  this.layout.file(this.provider { file })
