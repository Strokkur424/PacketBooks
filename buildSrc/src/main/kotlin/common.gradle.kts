import com.diffplug.spotless.LineEnding
import org.gradle.accessors.dm.LibrariesForLibs
import java.nio.charset.StandardCharsets

plugins {
  id("java-library")
  id("com.diffplug.spotless")
}

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://eldonexus.de/repository/maven-public/")
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
