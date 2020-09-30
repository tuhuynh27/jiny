---
home: true
heroImage: https://i.imgur.com/OpG00Ct.png
tagline: Lightweight, modern, simple Java HTTP Server/Client for rapid development in the API era
actionText: Quick Start →
actionLink: /guide/
features:
- title: Expressive
  details: Already know basic Java (or Kotlin/Scala)? Read the guide and start building things in no time (very few concepts that you need to learn)
- title: Lightweight
  details: Tiny-in-size with servlet-free (yeah, no servlet container) and no dependency, build and start are really fast, it also embeddable
- title: Performant
  details: Blazing fast routing performance with minimal optimization efforts and support asynchronous mode out-of-the-box
footer: Apache-2.0 Licensed | Copyright © 2020 Tu Huynh
---

<p style="text-align: right;"><img src="https://img.shields.io/github/workflow/status/huynhminhtufu/jiny/Java%20CI%20runner/master?label=test&amp;style=flat-square" alt="GitHub Workflow Status (branch)">
<img src="https://img.shields.io/tokei/lines/github/huynhminhtufu/jiny?style=flat-square" alt="Lines of code">
<img src="https://img.shields.io/github/languages/code-size/huynhminhtufu/jiny?style=flat-square" alt="GitHub code size in bytes">
<img src="https://img.shields.io/github/license/huynhminhtufu/jiny?style=flat-square" alt="GitHub">
<img src="https://img.shields.io/maven-central/v/com.jinyframework/jiny?style=flat-square" alt="Maven Central"></p>

You have your RESTful API ready to serve JSON in less than ten lines of code

:::: tabs

::: tab Java
 ```java
import com.jinyframework.*;

class Main {
    static void main() {
        val server = HTTPServer.port(1234);
        server.get("/ping", ctx -> HttpResponse.of("Pong"));
        server.start();
    }
} 
```
:::


::: tab Scala
```scala
import com.jinyframework.*

object Main extends App {
  val server = HttpServer.port(1234)
  server.get("/ping", ctx => HttpResponse.of("Pong))
  server.start()
}
```
:::

::: tab Kotlin
```kotlin
import com.jinyframework.*

fun main(args: Array<String>) {
    val server: HttpServer = HTTPServer.port(1234)
    server.get("/ping", ctx -> HttpResponse.of("Pong"))
    server.start()
}
```
:::

::::
