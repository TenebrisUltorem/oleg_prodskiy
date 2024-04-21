plugins {
    application
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.23"
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
}

group = "ru.vtb.szkf"
version = "1.0-SNAPSHOT"

application {
    mainClass = "ru.vtb.szkf.oleg.prodsky.MainKt"
    executableDir = ""
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // Telegram
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.1.0")

    // WebClient
    implementation("io.ktor:ktor-client-java:2.3.10")

    // Serialization
    implementation("com.charleskorn.kaml:kaml-jvm:0.58.0")

    // Logging
    implementation("ch.qos.logback:logback-core:1.5.5")
    implementation("ch.qos.logback:logback-classic:1.5.5")

    // Scheduling
    implementation("dev.inmo:krontab:2.2.9")

    // Database
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("org.jetbrains.exposed:exposed-core:0.49.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.49.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.49.0")

    // Test
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.10")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
