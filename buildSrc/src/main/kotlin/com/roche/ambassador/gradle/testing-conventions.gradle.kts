import com.roche.ambassador.gradle.utils.TestResultLogger
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.gradle.kotlin.dsl.support.serviceOf
import java.util.Optional.ofNullable

val junitVersion: String by extra
val testcontainersVersion: String by extra

plugins {
    id("java-conventions")
    id("idea")
    id("jacoco")
}

idea {
    module {
        sourceDirs.remove(file("src/integrationTest/kotlin"))
        testSourceDirs.add(file("src/integrationTest/kotlin"))
    }
}

fun Test.configure() {
    useJUnitPlatform()
    maxParallelForks = 2
    testLogging {
        events = setOf(PASSED, FAILED, SKIPPED)
        debug.events = setOf(STARTED, STANDARD_OUT)
        exceptionFormat = FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
    addTestListener(TestResultLogger(serviceOf()))
    finalizedBy(tasks.jacocoTestReport)
}

fun createTestTask(name: String, desc: String): Test {
    sourceSets {
        create(name) {
            compileClasspath += main.get().output + configurations.testRuntimeClasspath.get()
            runtimeClasspath += output + compileClasspath
        }
    }

    val testTask = task<Test>(name) {
        description = desc
        group = "verification"
        testClassesDirs = sourceSets[name].output.classesDirs
        classpath = sourceSets[name].runtimeClasspath
        configure()
    }

    tasks.check {
        dependsOn(testTask)
    }

    return testTask
}

tasks.test {
    configure()
}

val integrationTest = createTestTask("integrationTest", "Runs the integration tests")
val architectureTest = createTestTask("architectureTest", "Runs the integration tests")

tasks.jacocoTestReport {
    this.reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

fun ConfigurationContainer.gettingWithSources() = getting {
    extendsFrom(implementation.get())
    extendsFrom(testImplementation.get())
}

val integrationTestImplementation by configurations.gettingWithSources()
val architectureTestImplementation by configurations.gettingWithSources()

val ci by tasks.registering {
    description = "Runs the tests on CI based on flag"
    group = "verification"
    val testType = ofNullable(System.getenv("TEST_TYPE"))
        .map(String::toUpperCase)
        .orElse("ALL")
    when (testType) {
        "INTEGRATION" -> dependsOn(integrationTest)
        "UNIT" -> dependsOn(tasks.test.get())
        "ARCHITECTURE" -> dependsOn(architectureTest)
        else -> dependsOn(tasks.check.get())
    }
}

dependencies {
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("com.devskiller:jfairy:0.6.5")
    integrationTestImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    architectureTestImplementation("com.tngtech.archunit:archunit-junit5:0.23.1")
}
