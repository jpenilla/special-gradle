package xyz.jpenilla.specialgradle.task

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import xyz.jpenilla.specialgradle.Constants
import xyz.jpenilla.specialgradle.util.fileProvider
import xyz.jpenilla.specialgradle.util.sharedCache
import java.io.File
import java.net.URL
import java.time.Duration

/**
 * Update the file at [version] with the latest version number of BuildTools.
 */
public abstract class BuildToolsVersion : DefaultTask() {
  @get:OutputFile
  public val version: RegularFileProperty = this.project.objects.fileProperty()
    .convention(this.project.fileProvider(this.project.sharedCache(Constants.BUILD_TOOLS_VERSION_FILE_PATH)))

  @TaskAction
  private fun findVersion() {
    val currentFile: File = this.version.get().asFile
    currentFile.parentFile.mkdirs()
    val current = if (currentFile.exists()) currentFile.readText().toInt() else null

    val skip = this.project.gradle.startParameter.isOffline
      || !this.project.gradle.startParameter.isRefreshDependencies
      && current != null
      && System.currentTimeMillis() - currentFile.lastModified() < Duration.ofDays(1).toMillis()
    if (skip) {
      return
    }

    val latestBuild = try {
      val mapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .build()
      mapper.readValue<Response>(URL("https://hub.spigotmc.org/jenkins/job/BuildTools/lastBuild/api/json")).number
    } catch (ex: Exception) {
      this.logger.warn("Failed to fetch latest BuildTools version. Using latest locally cached version.", ex)
      current
    } ?: error("Failed to resolve latest local or remote version of BuildTools!")

    if (latestBuild != current) {
      currentFile.writeText("$latestBuild")
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private data class Response(val number: Int)
}
