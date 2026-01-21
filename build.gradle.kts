import xyz.jpenilla.runpaper.task.RunServer

plugins {
  id("common")
  alias(libs.plugins.run.paper)
}

dependencies {
  compileOnly(libs.paper.api)
  compileOnly(libs.caffeine)

  implementation(project("paper-abstract"))

  compileOnly(libs.strokk.commands.annotations)
  compileOnly(libs.strokk.config.annotations)

  annotationProcessor(libs.strokk.commands.processor)
  annotationProcessor(libs.strokk.config.processor)
}

tasks {
  jar {
    sequenceOf("abstract", "1-21-6").forEach {
      from(project(":paper-$it").sourceSets["main"].output)
    }
    manifest {
      attributes("Caffeine-Version" to libs.versions.caffeine)
    }
  }

  processResources {
    val props = mapOf("version" to project.version)
    filesMatching("paper-plugin.yml") {
      expand(props)
    }
  }
}

fun RunServer.configureServer() {
  minecraftVersion(libs.versions.minecraft.get())
  jvmArgs("-Xmx2G", "-Xms2G", "-Dcom.mojang.eula.agree=true")
}

runPaper.folia.registerTask {
  configureServer()
}

tasks.runServer {
  configureServer()
}
