plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "oop.barcelo.trackify27"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

application {
    mainClass.set("oop.barcelo.trackify27.HelloApplication")
}

javafx {

    version = "23.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation(platform("org.mongodb:mongodb-driver-bom:5.6.1"))
    implementation("org.mongodb:mongodb-driver-sync")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("org.slf4j:slf4j-simple:1.7.36")


    implementation("org.openjfx:javafx-controls:23.0.1")
    implementation("org.openjfx:javafx-fxml:23.0.1")

    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
