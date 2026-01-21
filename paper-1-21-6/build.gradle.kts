plugins {
  id("common")
  id("userdev")
}

userdev.version = "1.21.6-R0.1-SNAPSHOT"

dependencies.compileOnly(project(":paper-abstract"))
