# Installation

## Environment

- JDK 1.8 or higher
- Gradle/Maven build configuration

You can install via CLI or Build tool

## Start new Gradle project Jiny CLI

### Install Jiny CLI

For Unix (Linux/macOS) users, run:

```shell script
curl -o /usr/local/bin/jiny https://raw.githubusercontent.com/tuhuynh27/jiny/master/cli/binaries/macos/jinycli
chmod +x /usr/local/bin/jiny
```

For Windows users, please download the .exe file [here](https://github.com/tuhuynh27/jiny/blob/master/cli/binaries/windows/jinycli.exe).

### Run

Run and follow the CLI's instruction, after finish you will have a Gradle Java Project with Jiny included

```shell script
jiny
```

## Build tool

### Gradle Project (Recommended)

::: details I don't know Gradle, how to start with it?
[Gradle](https://gradle.org/) is a build tool for multi-language software development. It controls the development process in the tasks of compilation and packaging to testing, deployment, and publishing. Gradle is similar to Apache Ant/Apache Maven in term of Java build tools.

First, let's install Gradle.

If you're on Mac, with [brew](https://brew.sh/) simply run:
```shell script
brew install gradle
mkdir newproject && cd newproject
gradle init
```

If you're on Windows, with [choco](https://chocolatey.org/install) simple run:
```shell script
choco install gradle
mkdir newproject && cd newproject
gradle init
```

And then you have a Gradle build template with `build.gradle` file in your project directory `newproject`.
:::

`build.gradle`

```groovy
dependencies {
    compile group: 'com.jinyframework', name: 'core', version: '0.3.3'
}
```

### Maven Project

`pom.xml`

```xml
<dependency>
  <groupId>com.jinyframework</groupId>
  <artifactId>core</artifactId>
  <version>0.3.3</version>
</dependency>
```
