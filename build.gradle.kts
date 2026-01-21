import com.diffplug.spotless.LineEnding
import xyz.jpenilla.runpaper.task.RunServer
import java.nio.charset.StandardCharsets

plugins {
  id("java")
  alias(libs.plugins.run.paper)
  alias(libs.plugins.spotless)
  alias(libs.plugins.paperweight.userdev)
}

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://eldonexus.de/repository/maven-public/")
}

dependencies {
  paperweight.paperDevBundle(libs.versions.paper)
  compileOnly(libs.caffeine)

  compileOnly(libs.strokk.commands.annotations)
  compileOnly(libs.strokk.config.annotations)

  annotationProcessor(libs.strokk.commands.processor)
  annotationProcessor(libs.strokk.config.processor)
}

tasks {
  jar {
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

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

spotless {
  encoding = StandardCharsets.UTF_8
  lineEndings = LineEnding.UNIX

  java {
    trimTrailingWhitespace()
    endWithNewline()
    leadingTabsToSpaces(2)

    forbidWildcardImports()
    forbidModuleImports()
    removeUnusedImports()

    licenseHeaderFile(rootProject.file("HEADER"))
    target("**/*.java")
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

