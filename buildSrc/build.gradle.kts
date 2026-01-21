plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
}

dependencies {
  implementation(libs.plugin.spotless)
  implementation(libs.plugin.paperweight.userdev)

  // for version catalog inside convention plugin
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

kotlin {
  jvmToolchain(21)
}
