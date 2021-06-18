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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile
import org.gradle.kotlin.dsl.property
import xyz.jpenilla.specialgradle.Constants
import xyz.jpenilla.specialgradle.util.cmd
import xyz.jpenilla.specialgradle.util.sharedCache
import java.io.OutputStream
import java.security.MessageDigest

/**
 * Runs BuildTools to acquire mappings and dependencies.
 */
public abstract class BuildTools : JavaExec() {
  @get:Internal
  public val quiet: Property<Boolean> = this.project.objects.property<Boolean>().convention(false)

  @get:InputFile
  public abstract val buildToolsJar: RegularFileProperty

  @get:Input
  public abstract val minecraftVersion: Property<String>

  @get:OutputFile
  public val mojangMappingsTxt: RegularFileProperty = this.project.objects.fileProperty()
    .convention(this.outputFile("mojang-mappings.txt"))

  @get:OutputFile
  public val spigotMappingsCsrg: RegularFileProperty = this.project.objects.fileProperty()
    .convention(this.outputFile("spigot-mappings.csrg"))

  @get:OutputFile
  public val spigotMappedServer: RegularFileProperty = this.project.objects.fileProperty()
    .convention(this.outputFile("spigot.jar"))

  @get:OutputFile
  public val obfServer: RegularFileProperty = this.project.objects.fileProperty()
    .convention(this.outputFile("spigot-obf.jar"))

  @get:OutputFile
  public val mojangMappedServer: RegularFileProperty = this.project.objects.fileProperty()
    .convention(this.outputFile("spigot-mojang.jar"))

  override fun exec() {
    // configure JavaExec
    this.classpath(this.buildToolsJar)
    this.args(
      "--rev",
      this.minecraftVersion.get(),
      "--remapped",
      "--compile-if-changed"
    )
    this.workingDir(this.project.sharedCache("${Constants.BUILD_TOOLS_WORK_PATH}/${this.minecraftVersion.get()}"))
    this.workingDir.mkdirs()

    if (this.quiet.get()) {
      val nullOutputStream = object : OutputStream() {
        override fun write(byte: Int) {
          // no-op
        }
      }
      this.standardOutput = nullOutputStream
      this.errorOutput = nullOutputStream
    }

    // run BuildTools
    super.exec()

    val buildData = this.workingDir.resolve("BuildData")
    val buildDataCommit = cmd("git", "rev-parse", "HEAD", dir = buildData)
      .assertNormalExit()
      .message
      .trim()

    // gather meta and copy outputs

    val infoJsonFile = buildData.resolve("info.json")
    val mapper = JsonMapper.builder()
      .addModule(kotlinModule())
      .build()
    val info: Info = mapper.readValue(infoJsonFile)

    val mappingsVersion = MessageDigest.getInstance("MD5")
      .digest(buildDataCommit.toByteArray(Charsets.UTF_8))
      .asHexString()
      .substring(24) // Last 8 chars

    val spigotMappedServerFile = this.spigotMappedServer.get().asFile
    spigotMappedServerFile.parentFile.mkdirs()
    this.workingDir.resolve("spigot-${minecraftVersion.get()}.jar")
      .copyTo(spigotMappedServerFile, overwrite = true)

    val work = this.workingDir.resolve("work")

    val mojangMappingsTxtFile = this.mojangMappingsTxt.get().asFile
    mojangMappingsTxtFile.parentFile.mkdirs()
    work.resolve("minecraft_server.${this.minecraftVersion.get()}.txt")
      .copyTo(mojangMappingsTxtFile, overwrite = true)

    val spigotMappingsCsrgFile = this.spigotMappingsCsrg.get().asFile
    spigotMappingsCsrgFile.parentFile.mkdirs()
    work.resolve("bukkit-$mappingsVersion-combined.csrg")
      .copyTo(spigotMappingsCsrgFile, overwrite = true)

    val spigotTargetDir = this.workingDir.resolve("Spigot/Spigot-Server/target")

    val obfServerFile = this.obfServer.get().asFile
    obfServerFile.parentFile.mkdirs()
    spigotTargetDir.resolve("spigot-${info.spigotVersion}-remapped-obf.jar")
      .copyTo(obfServerFile, overwrite = true)

    val mojangMappedServerFile = this.mojangMappedServer.get().asFile
    mojangMappedServerFile.parentFile.mkdirs()
    spigotTargetDir.resolve("spigot-${info.spigotVersion}-remapped-mojang.jar")
      .copyTo(mojangMappedServerFile, overwrite = true)
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private data class Info(val spigotVersion: String)

  private fun ByteArray.asHexString(): String {
    val hexString = StringBuilder(2 * this.size)
    for (byte in this) {
      val hex = Integer.toHexString(0xff and byte.toInt())
      if (hex.length == 1) {
        hexString.append('0')
      }
      hexString.append(hex)
    }
    return hexString.toString()
  }

  private fun outputFile(name: String): Provider<RegularFile> =
    this.project.layout.file(this.minecraftVersion.map { mcVer ->
      this.project.sharedCache.resolve("${Constants.BUILD_TOOLS_OUTPUT_PATH}/$mcVer/$name")
    })
}
