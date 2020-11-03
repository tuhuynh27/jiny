# Production Suggestions

## Logging

Recommended to use [logback](http://logback.qos.ch/)

`build.gradle`

```groovy
dependencies {
    compile group: 'ch.qos.logback', name:'logback-classic', version: '1.0.9'
    compile group: 'ch.qos.logback', name:'logback-core', version: '1.0.9'
}
```

## Build

Suggestion: use [com.github.johnrengelman.shadow](https://github.com/johnrengelman/shadow) to build Gradle project to JAR executable file:

`build.gradle`

```groovy
plugins {
    id 'com.github.johnrengelman.shadow' version '6.0.0'
}
```

## Boilerplates

[See Examples](https://github.com/huynhminhtufu/jiny/tree/master/examples)

## Health Checks and Graceful Shutdown

Add shutdown hook

`Main.java`

```java
Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
log.info("Added shutdown hook");
```

:::warning WIP
This page is in WIP
:::
