package cmd

const BuildGradle = `plugins {
    // Apply the java plugin to add support for Java
    id 'java'

    // Apply the application plugin to add support for building a CLI application.
    id 'application'

	// Shadow Jar plugin
    id 'com.github.johnrengelman.shadow' version '6.0.0'
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
}

dependencies {
    compileOnly 'org.projectlombok:lombok:1.18.12'
    annotationProcessor 'org.projectlombok:lombok:1.18.12'

    compile group: 'com.jinyframework', name: 'core', version: '0.3.2'
    compile group: 'com.google.code.gson', name: 'gson', version: '2.8.6'
    compile group: 'ch.qos.logback', name:'logback-classic', version: '1.0.9'
    compile group: 'ch.qos.logback', name:'logback-core', version: '1.0.9'
}

application {
    // Define the main class for the application.
    mainClassName = '{{ .MainClassName }}'
}
`

const SettingsGradle = `rootProject.name = '{{ .ArtifactName }}'
`