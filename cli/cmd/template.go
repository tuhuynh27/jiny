package cmd

const BuildGradleTemplate = `plugins {
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
	{{ if eq .Feature 1 }}
    compile group: 'com.google.code.gson', name: 'gson', version: '2.8.6'
	{{ end }}
	{{ if eq .Feature 2 }}
	compile group: 'com.google.code.gson', name: 'gson', version: '2.8.6'
	compile group: 'com.jinyframework', name: 'cors-middleware', version: '0.3.2'
	compile group: 'com.jinyframework', name: 'jwt-middleware', version: '0.3.2'
	{{ end }}
    compile group: 'ch.qos.logback', name:'logback-classic', version: '1.0.9'
    compile group: 'ch.qos.logback', name:'logback-core', version: '1.0.9'

	// Use JUnit test framework
    testImplementation 'junit:junit:4.13'
}

application {
    // Define the main class for the application.
    mainClassName = '{{ .SourcePackage }}.App'
}
`

const SettingsGradleTemplate = `rootProject.name = '{{ .ProjectName }}'
`

const AppTemplate = `package {{ .SourcePackage }};

import com.jinyframework.*;
import static com.jinyframework.core.AbstractRequestBinder.HttpResponse.of;

import com.google.gson.Gson;

import lombok.val;

public class App {
    public static void main(String[] args) {
		{{if eq .Feature 0 -}}
        val server = HttpServer.port(1234);
		{{- end}}
		{{if eq .Feature 1 -}}
		val gson = new Gson();
		val server = HttpServer.port(1234)
                               .useTransformer(gson::toJson);
		{{- end}}
		{{if eq .Feature 2 -}}
		val gson = new Gson();
		val server = HttpServer.port(1234)
                               .useTransformer(gson::toJson);
		{{- end}}
		{{if eq .Feature 2 -}}
		server.use(Cors.newHandler(Cors.allowDefault());
		{{- end}}
        server.get("/hello", ctx -> of("Hello World!"));
        server.start();
    }
}
`

const AppTestTemplate = `package {{ .SourcePackage }};

import org.junit.Test;
import static org.junit.Assert.*;

public class AppTest {
    @Test public void testAppHasAGreeting() {
        App classUnderTest = new App();
        assertNotNull("app should have a greeting", classUnderTest.getGreeting());
    }
}
`