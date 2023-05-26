plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "io.github.mynameisjuliano"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jsoup:jsoup:1.15.4")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("commons-cli:commons-cli:1.5.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }

    configurations["compileClasspath"].forEach { file : File ->
        from(zipTree(file.absoluteFile))
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}