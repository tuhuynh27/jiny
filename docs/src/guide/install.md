# Installation

## Environment

- JDK 1.8 or higher
- Gradle/Maven build configuration

## Build tool

### Gradle Project (Recommended)

::: details I don't know Gradle, how to start with it?
First, [install Gradle](https://gradle.org/install/), if you're on Mac, simply run:
```shell script
brew install gradle
gradle init
```
And then you have a Gradle build template with `build.gradle` file
:::

`build.gradle`

```groovy
dependencies {
    compile group: 'com.jinyframework', name: 'jiny', version: '0.2.6'
}
```

### Maven Project

`pom.xml`

```xml
<dependency>
  <groupId>com.jinyframework</groupId>
  <artifactId>jiny</artifactId>
  <version>0.2.6</version>
</dependency>
```
