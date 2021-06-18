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

public object Constants {
  public const val SPECIAL_SOURCE_CONFIG: String = "specialSource"
  public const val MOJANG_MAPPED_SERVER_CONFIG: String = "mojangMappedServer"

  public const val SPECIAL_GRADLE_EXTENSION_NAME: String = "specialGradle"

  public const val TASK_GROUP: String = "special gradle"
  public const val OBF_JAR_TASK_NAME: String = "obfJar"
  public const val PRODUCTION_MAPPED_JAR_TASK_NAME: String = "productionMappedJar"
  public const val DOWNLOAD_BUILD_TOOLS_TASK_NAME: String = "downloadBuildTools"
  public const val FETCH_BUILD_TOOLS_VERSION_TASK_NAME: String = "fetchBuildToolsVersion"
  public const val BUILD_TOOLS_TASK_NAME: String = "buildTools"
  public const val CLEAN_SPECIAL_GRADLE_CACHE_TASK_NAME: String = "cleanSpecialGradleCache"

  public const val CACHE_PATH: String = "caches"
  public const val SPECIAL_GRADLE_PATH: String = "special-gradle"
  public const val TASK_CACHE_PATH: String = "$SPECIAL_GRADLE_PATH/task-cache"
  public const val BUILD_TOOLS_JAR_PATH: String = "$SPECIAL_GRADLE_PATH/buildtools.jar"
  public const val BUILD_TOOLS_VERSION_FILE_PATH: String = "$SPECIAL_GRADLE_PATH/buildtools-version.txt"
  public const val BUILD_TOOLS_PATH: String = "$SPECIAL_GRADLE_PATH/buildtools"
  public const val BUILD_TOOLS_WORK_PATH: String = "$BUILD_TOOLS_PATH/work"
  public const val BUILD_TOOLS_OUTPUT_PATH: String = "$BUILD_TOOLS_PATH/out"

  public const val BUILD_TOOLS_DOWNLOAD_LINK: String = "https://hub.spigotmc.org/jenkins/job/BuildTools/{}/artifact/target/BuildTools.jar"
}
