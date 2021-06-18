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

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

/**
 * Extension exposing configuration for Special Gradle.
 *
 * @param objects object factory
 */
public abstract class SpecialGradleExtension @Inject constructor(objects: ObjectFactory) {
  /**
   * The Minecraft version for running BuildTools and injecting the Mojang-mapped Spigot dependency.
   */
  public val minecraftVersion: Property<String> = objects.property()

  /**
   * Whether to inject repositories for dependency resolution.
   *
   * Currently, this only adds the Maven Central repo for resolving SpecialSource.
   */
  public val injectRepositories: Property<Boolean> = objects.property<Boolean>().convention(true)

  /**
   * The version of SpecialSource to use for remapping.
   */
  public val specialSourceVersion: Property<String> = objects.property()

  /**
   * Whether to inject a dependency on Mojang-mapped Spigot.
   */
  public val injectSpigotDependency: Property<Boolean> = objects.property<Boolean>().convention(true)
}
