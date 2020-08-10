plugins {
    id("java-library")
    id("com.github.hierynomus.license")
    id("com.github.sgtsilvio.gradle.utf8")
    id("com.github.sgtsilvio.gradle.metadata")
    id("com.github.sgtsilvio.gradle.javadoc-links")
}

/* ******************** metadata ******************** */

group = "com.hivemq"
description = "Hello World Customization for the HiveMQ Enterprise Extensions for Kafka"

metadata {
    readableName = "HiveMQ Kafka Extension Customization SDK"
    organization {
        name = "HiveMQ GmbH"
        url = "https://www.hivemq.com/"
    }
    license {
        apache2()
    }
    developers {
        developer {
            id = "cschaebe"
            name = "Christoph Schaebel"
            email = "christoph.schaebel@hivemq.com"
        }
        developer {
            id = "lbrandl"
            name = "Lukas Brandl"
            email = "lukas.brandl@hivemq.com"
        }
        developer {
            id = "flimpoeck"
            name = "Florian Limpoeck"
            email = "florian.limpoeck@hivemq.com"
        }
        developer {
            id = "sauroter"
            name = "Georg Held"
            email = "georg.held@hivemq.com"
        }
        developer {
            id = "SgtSilvio"
            name = "Silvio Giebl"
            email = "silvio.giebl@hivemq.com"
        }
    }
    github {
        org = "hivemq"
        repo = "hivemq-kafka-hello-world-customization"
        issues()
    }
}

/* ******************** dependencies ******************** */

repositories {
    mavenCentral()
}

dependencies {
    api("com.hivemq:hivemq-kafka-extension-customization-sdk:${property("hivemq-kakfa-sdk.version")}")
    api("org.slf4j:slf4j-api:${property("slf4j.version")}")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit.jupiter.version")}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${property("junit.jupiter.version")}")
    testImplementation("org.mockito:mockito-core:${property("mockito.version")}")
    testRuntimeOnly("org.mockito:mockito-junit-jupiter:${property("mockito.version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junit.jupiter.version")}")
}

tasks.test {
    useJUnitPlatform()
}

/* ******************** java ******************** */

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Jar>().configureEach {
    manifest.attributes(
            "Implementation-Title" to project.name,
            "Implementation-Vendor" to metadata.organization.name,
            "Implementation-Version" to project.version)
}

tasks.javadoc {
    title = "${metadata.readableName} ${project.version} API"
}

/* ******************** checks ******************** */

license {
    header = file("${projectDir}/HEADER")
    mapping("java", "SLASHSTAR_STYLE")
}
