plugins {
    id("java")
    alias(libs.plugins.run.paper)
    alias(libs.plugins.spotless)
    alias(libs.plugins.blossom)
}

group = "net.strokkur"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.caffeine)

    compileOnly(libs.strokk.commands.annotations)
    compileOnly(libs.strokk.config.annotations)

    annotationProcessor(libs.strokk.commands.processor)
    annotationProcessor(libs.strokk.config.processor)
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER"))
        target("**/*.java")
    }
}

sourceSets.main {
    blossom.javaSources {
        property("caffeine", libs.versions.caffeine.get())
    }
}

tasks {
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        jvmArgs("-Xmx2G", "-Xms2G", "-Dcom.mojang.eula.agree=true")
    }
}

