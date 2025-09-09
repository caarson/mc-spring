plugins {
    id("java")
}

group = "com.neptune"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
}

dependencies {
    implementation("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    // Optional dependencies for hooks
    compileOnly("me.clip.placeholderapi:PlaceholderAPI:2.10.8") // soft-depend
    compileOnly("com.sk89q.worldguard:worldguard-core:7.0.6") // soft-depend
    compileOnly("net.luckperms.api:luckperms-api:5.3.4") // soft-depend
    implementation("org.xerial:sqlite-jdbc:3.21.0") // for sqlite storage if needed
}
