<p>
    <a href="https://jinyframework.com" target="_blank">
        <img alt="Jiny Framework" src="https://i.imgur.com/viYCs8l.png" />
    </a>
</p>

![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/huynhminhtufu/jiny/Java%20CI%20runner/master?label=build&style=flat-square)
![Lines of code](https://img.shields.io/tokei/lines/github/huynhminhtufu/jiny?style=flat-square)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/huynhminhtufu/jiny?style=flat-square)
![GitHub](https://img.shields.io/github/license/huynhminhtufu/jiny?style=flat-square)
![Maven Central](https://img.shields.io/maven-central/v/com.jinyframework/core?style=flat-square)

## What is Jiny?

**Jiny** features a **lightweight (Servlet-free and no dependency), expressive and unopinionated** pure HTTP Server/Client including request parser, routing, middlewares, proxy-mode and more. If you need a quick start & simple way to write a JVM server, you would love this framework. Unlike others web frameworks, you can structure your application as you want.

## Quick Start

[Get started in 5 minutes.](https://jinyframework.com)

## Changelogs

[Learn about the latest improvements.](https://jinyframework.com/guide/changelogs.html)

## Development

Want to file a bug, contribute some code, or improve documentation? Excellent!

First, [see Developer Guide](https://jinyframework.com/guide/developer-guide.html).

Pull requests are encouraged and always welcome. [Pick an issue](https://github.com/huynhminhtufu/jiny/issues) and help us out!

To install and work on Jiny locally:

```
$ git clone https://github.com/huynhminhtufu/jiny.git
$ cd jiny
$ ./gradlew dependencies
```

To build the modules included in the package:

```
$ ./gradlew --parallel build
```

Running tests:

```
$ ./gradlew test
```

## License

[Apache License 2.0](https://github.com/huynhminhtufu/jiny/blob/master/LICENSE)