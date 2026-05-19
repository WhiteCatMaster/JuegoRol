plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.jetbrains.kotlin.plugin.jpa") version "2.2.21"
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.dokka") version "2.2.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    jacoco
}

group = "org.example"
version = "0.0.1-SNAPSHOT"
description = "Backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("com.google.firebase:firebase-admin:9.2.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}


kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

// 1. Configuración común para todos los tests
tasks.withType<Test> {
    useJUnitPlatform()
}

// 2. Configuración de la tarea 'test' (Unitarios)
tasks.named<Test>("test") {
    description = "Ejecuta las pruebas unitarias."
    // Excluye las pruebas de integración
    exclude("**/integration/**")

    // Al terminar, genera el informe de JaCoCo
    finalizedBy(tasks.jacocoTestReport)
}

// 3. Creación de la tarea 'integrationTest' (Integración)
tasks.register<Test>("integrationTest") {
    description = "Ejecuta las pruebas de integración."
    group = "verification"

    // Configura la tarea para que encuentre tus clases de test
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    // Filtra para que solo ejecute lo que esté en la carpeta de integración
    include("**/integration/**")

    // Orden lógico: primero unitarios, luego integración
    shouldRunAfter(tasks.test)

    // Al terminar, también genera el informe de JaCoCo
    finalizedBy(tasks.jacocoTestReport)
}

// 4. Configuración de JaCoCo para unir ambos niveles de test
tasks.jacocoTestReport {
    // El informe depende de que se hayan ejecutado los tests
    dependsOn(tasks.test, tasks.named("integrationTest"))

    // Recopila los datos de ejecución de todas las tareas de test
    executionData(fileTree(project.layout.buildDirectory).include("jacoco/*.exec"))

    reports {
        html.required.set(true) // Informe visual para humanos
        xml.required.set(true)  // Informe para herramientas de CI/CD
    }
    classDirectories.setFrom(files(classDirectories.files.map {
        fileTree(it).exclude(
            // Rutas de paquetes a ignorar (usa ant-style patterns)
            "**/org/example/backend/dto/**",
            "**/org/example/backend/entity/**",
            "**/org/example/backend/config/**",
            "**/org/example/backend/observability/**",
            "**/org/example/backend/repository/**",

            // Clases específicas
            "**/org/example/backend/BackendApplication*",

            // Si usas QueryDSL o código generado, también puedes quitarlo aquí
            "**/generated/**"
        )
    }))
}

// 5. Vincular todo al ciclo de vida 'check' de Gradle
tasks.named("check") {
    dependsOn("integrationTest")
}