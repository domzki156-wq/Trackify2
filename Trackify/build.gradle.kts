plugins {
    java
    application
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

javafx {
    version = "23.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("App.MainApp")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // JSON
    implementation("org.json:json:20230227")

    // MongoDB driver
    implementation("org.mongodb:mongodb-driver-sync:4.11.0")

    // favre bcrypt (matches your AuthService import)
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
