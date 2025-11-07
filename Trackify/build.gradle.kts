plugins {
    java
    application
    // use the stable OpenJFX Gradle plugin version that integrates with Kotlin DSL
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "com.trackify"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

// JavaFX plugin config
javafx {
    // Keep a JavaFX version compatible with your JDK; 20/21/22/23 are fine.
    version = "23.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("App.MainApp") // fully-qualified main class
}

dependencies {
    // JUnit for testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // MongoDB Java driver (sync)
    implementation("org.mongodb:mongodb-driver-sync:4.11.0")

    // (optional) BCrypt if you need it later
    implementation("at.favre.lib:bcrypt:0.9.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "App.MainApp"
    }
}

// create exports dir before run
tasks.register("createExportsDir") {
    doLast {
        val exportsDir = file("exports")
        if (!exportsDir.exists()) {
            exportsDir.mkdirs()
            println("Created exports directory: ${exportsDir.absolutePath}")
        }
    }
}

tasks.named("run") {
    dependsOn("createExportsDir")
}
