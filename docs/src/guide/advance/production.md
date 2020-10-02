# Production Use

## Logging

Recommended to use logback

```groovy
dependencies {
    compile group: 'ch.qos.logback', name:'logback-classic', version: '1.0.9'
    compile group: 'ch.qos.logback', name:'logback-core', version: '1.0.9'
}
```

## Build

Suggestion: use `com.github.johnrengelman.shadow` to build Gradle project to JAR executable file:

`build.gradle`

```groovy
plugins {
    id 'com.github.johnrengelman.shadow' version '5.0.0'
}

shadowJar {
    classifier = 'fat'
}
```

## Boilerplates

[See Examples](https://github.com/huynhminhtufu/jiny/tree/master/examples)

## Health Checks and Graceful Shutdown

Add shutdown hook

```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }));
        log.info("Added shutdown hook");
```

(WIP)
