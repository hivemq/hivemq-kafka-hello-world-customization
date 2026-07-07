plugins {
    java
    alias(libs.plugins.defaults)
    alias(libs.plugins.spotless)
}

group = "com.hivemq.extensions.kafka.customizations"
description = "Hello World Customization for the HiveMQ Enterprise Extensions for Kafka"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.compileJava {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.hivemq.kafkaExtension.customizationSdk)
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        "test"(JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit.jupiter)
            dependencies {
                implementation(libs.mockito)
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}

tasks.withType<Jar>().configureEach {
    manifest.attributes(
        "Implementation-Title" to project.name,
        "Implementation-Vendor" to "HiveMQ GmbH",
        "Implementation-Version" to project.version,
    )
}

spotless {
    java {
        licenseHeaderFile(rootDir.resolve("HEADER"))
    }
}
