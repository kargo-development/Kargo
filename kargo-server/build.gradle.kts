
val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
    id("org.graalvm.buildtools.native") version "0.11.3"

}

group = "dev.kargo.server"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}
dependencies {
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.github.flaxoos:ktor-server-rate-limiting:2.2.1")
    implementation("com.ucasoft.ktor:ktor-simple-cache:0.55.3")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposed_version")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
    implementation("io.insert-koin:koin-ktor:4.2.0-RC1")
    implementation("io.insert-koin:koin-logger-slf4j:4.2.0-RC1")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-auth")

    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}


graalvmNative {
    toolchainDetection.set(true)

    binaries {
        named("main") {
            resources.autodetect()

            buildArgs.addAll(
                listOf(
                    "--no-fallback",
                    "-H:+AddAllCharsets",
                    "-H:+ReportExceptionStackTraces",

                    // Resources
                    "-H:IncludeResources=templates/.*",
                    "-H:IncludeResources=static/.*",
                    "-H:IncludeResources=.*\\.yaml$",
                    "-H:IncludeResources=.*\\.conf$",

                    // SQLite
                    "--initialize-at-run-time=org.sqlite.JDBC",
                    "--initialize-at-run-time=org.sqlite.core.NativeDB",

                    // Security & Random
                    "--initialize-at-run-time=java.security.SecureRandom",
                    "--initialize-at-run-time=sun.security.provider.NativePRNG",

                    // kotlinx.serialization (SAFE ONLY)
                    "--initialize-at-build-time=kotlinx.serialization.json.ClassDiscriminatorMode",
                    "--initialize-at-build-time=kotlinx.serialization.json.JsonConfiguration",
                    "--initialize-at-build-time=kotlinx.serialization.json.internal.DescriptorSchemaCache"
                )
            )

            mainClass.set("io.ktor.server.netty.EngineMain")
        }
    }
}