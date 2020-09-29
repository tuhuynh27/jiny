# Installation

## Environment
- JDK 1.8 or higher
- Gradle/Maven build configuration

## Gradle Project

::: details Why use GitHubPackages
The framework is being hosted on Github Package now, so it requires a Github Authentication to download it, soon it will be moved to Maven Central :smirk:
:::

`build.gradle`

```groovy
repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/huynhminhtufu/jiny")
        credentials {
            username = project.findProperty("github.user") ?: System.getenv("USERNAME")
            password = project.findProperty("github.token") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    compile group: 'com.jinyframework', name: 'jiny', version: '0.2.2'
}
```

### Github Token

You need to modify the `gradle.properties` like this:

```text
github.user=huynhminhtufu
github.token=your_github_token
```

As `github.user` is your Github Username, and `github.token` is your Github Token (you can get token [here](https://github.com/settings/tokens))

::: warning
Remember to add `gradle.properties` to your `.gitignore` file
:::

## Maven Project

`pom.xml`

```xml
<dependency>
  <groupId>com.jinyframework</groupId>
  <artifactId>jiny</artifactId>
  <version>0.2.2</version>
</dependency>
```
