import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode

plugins {
    kotlin("jvm") version "2.2.21"
    id("com.diffplug.spotless") version "8.1.0"
}

group = "com.github.ian4hu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    //implementation("com.baomidou:mybatis-plus-boot-starter:3.5.15")
    implementation("com.baomidou:mybatis-plus:3.5.15")
    implementation("org.springframework:spring-aop:5.3.39")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_9
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        targetCompatibility = JavaVersion.VERSION_1_9
        sourceCompatibility = JavaVersion.VERSION_1_9
    }
}

spotless {
    ratchetFrom("origin/main")
    encoding("UTF-8")

    java {
        importOrder("java", "javax", "com", "org")
		eclipse().configFile(rootProject.file("eclipse-format-file.xml"))
		trimTrailingWhitespace()
        formatAnnotations()
        licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
    }

    kotlin {
        ktlint()
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
        licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
    }
}

tasks.compileKotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_9
    }
}

tasks.compileTestKotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_9
    }
}

tasks.test {
    useJUnitPlatform()
}