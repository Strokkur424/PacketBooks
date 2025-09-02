plugins {
    id("java")
    alias(libs.plugins.run.paper)
    alias(libs.plugins.blossom)
}

group = "net.strokkur"
version = "1.0.0"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.caffeine)
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

