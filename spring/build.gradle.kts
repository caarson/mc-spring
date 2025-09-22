plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.neptune"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://repo.luckperms.net/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    // Optional dependencies for hooks
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
    compileOnly("net.luckperms:api:5.4")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    
    shadowJar {
        archiveBaseName.set("Spring")
        archiveClassifier.set("")
        relocate("com.fasterxml.jackson", "com.neptune.spring.lib.jackson")
        relocate("org.xerial.sqlite", "com.neptune.spring.lib.sqlite")
    }
    
    build {
        dependsOn(shadowJar)
    }
}
