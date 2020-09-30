# Production Use

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

(WIP)
