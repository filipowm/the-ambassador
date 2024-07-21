import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("spring-conventions")
    id("jooq-conventions")
    id("org.jetbrains.kotlin.plugin.jpa") version "1.6.0"
}

tasks.getByName("sourcesJar") {
    dependsOn("generateJooq")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<BootBuildImage>("bootBuildImage") {
    enabled = false
}

tasks.getByName<BootRun>("bootRun") {
    enabled = false
}

val jacksonVersion: String by extra
val postgresqlDriverVersion: String by extra
val testcontainersVersion: String by extra
val jooqVersion: String by extra

dependencies {

    implementation(project(":ambassador-model"))
    api(project(":ambassador-commons"))
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core:8.5.13")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.jooq:jooq:$jooqVersion")
    api("com.vladmihalcea:hibernate-types-52:2.16.2")
    implementation("org.jooq:jooq-meta-extensions-hibernate:$jooqVersion") {
        exclude("com.h2database", "h2")
    }
    runtimeOnly("org.postgresql:postgresql:$postgresqlDriverVersion")

    jooqGenerator("org.testcontainers:postgresql:$testcontainersVersion")
    jooqGenerator("org.postgresql:postgresql:$postgresqlDriverVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.ttddyy:datasource-proxy:1.7")
    testImplementation(project(":ambassador-fake-source"))
}

jooq {
    configuration {
        jdbc {
            username = "postgres"
            password = "postgres"
            driver = "org.testcontainers.jdbc.ContainerDatabaseDriver"
            url =
                "jdbc:tc:postgresql:14:///ambassador?TC_TMPFS=/testtmpfs:rw&amp;TC_INITFUNCTION=com.roche.ambassador.gradle.utils.DatabaseInit::flyway"
        }
        generator {
            target {
                packageName = "com.roche.ambassador.storage.jooq"
                directory = "build/generated-src/jooq/main"
            }
            database {
                inputSchema = "public"
            }
            generate {
                isImmutablePojos = true
                isPojosEqualsAndHashCode = true
                isFluentSetters = true
                isJavaTimeTypes = true
                isDeprecationOnUnknownTypes = false
            }
        }
    }
}

description = "ambassador-storage"
