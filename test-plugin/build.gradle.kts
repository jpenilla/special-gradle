import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder

plugins {
  // special gradle
  id("xyz.jpenilla.special-gradle")

  // other stuff
  `java-library`
  id("net.minecrell.plugin-yml.bukkit")
  id("net.kyori.indra")
  id("net.kyori.indra.license-header")
  id("xyz.jpenilla.run-paper")
}

group = "xyz.jpenilla"
version = "1.0.0-SNAPSHOT"

specialGradle {
  // set special source version
  specialSourceVersion.set("1.10.0")
}

// spigot and mappings
dependencies {
  val spigotVer = "1.17-R0.1-SNAPSHOT"
  // add mojang mapped spigot to compileOnly
  compileOnly(group = "org.spigotmc", name = "spigot", version = spigotVer, classifier = "remapped-mojang")
  // add mappings txt installed to maven local by build tools to reobfMappings
  reobfMappings(group = "org.spigotmc", name = "minecraft-server", version = spigotVer, ext = "txt", classifier = "maps-mojang")
}

// set reobfJar to run on build
tasks {
  build {
    dependsOn(reobfJar)
  }
}


// further lines past here are unrelated to special-gradle setup

// indra config
indra {
  javaVersions().target(16)
}

// license config
license {
  header("../LICENSE_HEADER")
}

// plugin.yml
bukkit {
  load = PluginLoadOrder.STARTUP
  main = "xyz.jpenilla.specialgradle.testplugin.TestPlugin"
  apiVersion = "1.17"
}

tasks {
  runServer {
    minecraftVersion("1.17")
  }
}
