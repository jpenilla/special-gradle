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

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URL
import java.nio.channels.Channels

/**
 * Attempts to download the file at [remoteURL] to [destination].
 */
public abstract class DownloadFile : DefaultTask() {
  @get:Input
  public abstract val remoteURL: Property<URL>

  @get:OutputFile
  public abstract val destination: RegularFileProperty

  @TaskAction
  private fun download() {
    val remote = this.remoteURL.get()
    this.destination.get().asFile.parentFile.mkdirs()
    remote.openStream().use { stream ->
      this.destination.get().asFile.outputStream().use { out ->
        out.channel.transferFrom(Channels.newChannel(stream), 0, Long.MAX_VALUE)
      }
    }
  }
}
