import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "dev.masa"
version = "1.0-SNAPSHOT"

plugins {
    `java`
    kotlin("jvm") version "1.5.0-RC"

    id("com.github.johnrengelman.shadow") version "6.1.0"
}

java {
    sourceCompatibility = JavaVersion.toVersion(16)
    targetCompatibility = JavaVersion.toVersion(16)
}

allprojects {

    group = project.group
    version = project.version

    repositories {
        mavenLocal()
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.16"
            useIR = true
        }
    }

}
