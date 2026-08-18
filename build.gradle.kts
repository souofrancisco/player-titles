plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

group = "dev.souofrancisco"
version = "0.1.0"
description = "A minimal Folia-compatible player titles plugin foundation."

val lombokVersion = "1.18.46"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
    compileOnly("dev.folia:folia-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("dev.jorel:commandapi-paper-core:11.1.0")

    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")

    implementation("xyz.xenondevs.invui:invui:1.49")
    implementation("com.zaxxer:HikariCP:7.1.0") {
        exclude(group = "org.slf4j")
    }
    implementation("org.xerial:sqlite-jdbc:3.53.2.1")
}

tasks.matching { it.name in setOf("compileTestJava", "processTestResources", "testClasses", "test") }.configureEach {
    enabled = false
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "description" to project.description,
    )

    inputs.properties(props)

    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveBaseName.set("player-titles")
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
}

tasks.jar {
    enabled = false
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.register<Copy>("deploy") {
    group = "deployment"
    description = "Builds the plugin and copies the shaded jar into ../test-server/plugins/ when it exists."

    dependsOn(tasks.shadowJar)
    mustRunAfter(tasks.check)

    val serverPluginsDirectory = layout.projectDirectory.dir("../test-server/plugins")
    onlyIf {
        serverPluginsDirectory.asFile.isDirectory
    }

    from(tasks.shadowJar.flatMap { it.archiveFile })
    into(serverPluginsDirectory)

    doFirst {
        logger.lifecycle("Deploying PlayerTitles to ${serverPluginsDirectory.asFile}")
    }
}

tasks.build {
    dependsOn(tasks.named("deploy"))
}
