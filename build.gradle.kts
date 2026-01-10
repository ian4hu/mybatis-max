import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode

plugins {
    kotlin("jvm") version "2.2.21"
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