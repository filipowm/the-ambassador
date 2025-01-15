
//
//for (String key : properties.stringPropertyNames()) {
//    ext.set(key, properties.getProperty(key))
//}
//val detektVersion : String by extra
//val jooqVersion : String by extra

plugins {
    // Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
    mavenCentral()

    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap/")
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("org.jetbrains.kotlin:kotlin-allopen")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:2.6.7")
    implementation("org.jooq:jooq-codegen:3.19.18")
    implementation("org.flywaydb:flyway-core:8.5.9")

//    implementation("org.testcontainers:postgresql:1.15.2") // TODO remove
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.6.20")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.20.0")
}
