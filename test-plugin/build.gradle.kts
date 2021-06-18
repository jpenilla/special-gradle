import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder
import xyz.jpenilla.specialgradle.task.RemapJar

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
  // set Minecraft version for running BuildTools and injecting Spigot dependency
  minecraftVersion.set("1.17")
  // set SpecialSource version
  specialSourceVersion.set("1.10.0")
}

tasks {
  // set productionMappedJar to run on build
  build {
    dependsOn(productionMappedJar)
  }

  // optionally silence log output from BuildTools and SpecialSource
  buildTools {
    quiet.set(true)
  }
  withType<RemapJar> {
    quiet.set(true)
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

// configure run-paper
runPaper {
  disablePluginJarDetection()
}

tasks {
  runServer {
    minecraftVersion("1.17")
    pluginJars.from(productionMappedJar)
  }
}
