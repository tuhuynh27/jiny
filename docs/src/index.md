---
home: true
heroImage: https://i.imgur.com/OpG00Ct.png
tagline: Lightweight, modern, simple Java web framework for rapid development in the API era
actionText: Quick Start →
actionLink: /guide/
features:
- title: Expressive
  details: Already know basic Java (or Kotlin/Scala)? Read the guide and start building things in no time (no implicit annotation and very few concepts to learn)
- title: Lightweight
  details: Tiny-in-size (core is just 50kb) with servlet-free (thus no need embedded servlet containers) and no dependency, build and start are really fast
- title: Performant
  details: Blazing fast performance due to the simplicity, also support asynchronous mode out-of-the-box (to handle a lot of concurrency with minimal hardware)
footer: Apache-2.0 Licensed | Copyright © 2020 Jiny Team
---

<p style="text-align: right; max-width: 960px; margin: auto;">
<img src="https://img.shields.io/github/workflow/status/huynhminhtufu/jiny/Java%20CI%20runner/master?label=build&amp;style=flat-square" alt="GitHub Workflow Status (branch)">
<img src="https://img.shields.io/tokei/lines/github/huynhminhtufu/jiny?style=flat-square" alt="Lines of code">
<img src="https://img.shields.io/github/languages/code-size/huynhminhtufu/jiny?style=flat-square" alt="GitHub code size in bytes">
<img src="https://img.shields.io/github/license/huynhminhtufu/jiny?style=flat-square" alt="GitHub">
<img src="https://img.shields.io/maven-central/v/com.jinyframework/core?style=flat-square" alt="Maven Central">
</p>

<p style="max-width: 960px; margin: auto; margin-top: 1rem; text-align: center;">You have your RESTful API server
 ready to serve
 JSON in
 less
 than ten
 lines of code</p>

:::: tabs

::: tab Java
 ```java
import com.jinyframework.*;

class Main {
   static void main(String[] args) {
       HttpServer server = HttpServer.port(1234);
       server.get("/hello", ctx -> of("Hello World!"));
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
  server.get("/hello", ctx => of("Hello World!"))
  server.start()
}
```
:::

::: tab Kotlin
```kotlin
import com.jinyframework.*

fun main(args: Array<String>) {
    val server: HttpServer = HttpServer.port(1234)
    server.get("/hello", ctx -> of("Hello World!"))
    server.start()
}
```
:::

::::

<div style="text-align: center;">
    <svg style="transform: rotate(90deg); width: 1.5rem; height: 1.5rem;" aria-hidden="true" focusable="false" data-prefix="fas" data-icon="arrow-right" class="svg-inline--fa fa-arrow-right fa-w-14 " role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512"><path fill="currentColor" d="M190.5 66.9l22.2-22.2c9.4-9.4 24.6-9.4 33.9 0L441 239c9.4 9.4 9.4 24.6 0 33.9L246.6 467.3c-9.4 9.4-24.6 9.4-33.9 0l-22.2-22.2c-9.5-9.5-9.3-25 .4-34.3L311.4 296H24c-13.3 0-24-10.7-24-24v-32c0-13.3 10.7-24 24-24h287.4L190.9 101.2c-9.8-9.3-10-24.8-.4-34.3z"></path></svg>
</div>

<div style="text-align: center;">
    <img src="https://i.imgur.com/feIt8al.png" style="max-width: 100%" />
</div>

<section class="generic-block">
    <div class="mid">
        <div class="left">
            <h3>Robust Routing</h3>
            <p>
                Setting up routes for your application has never been so easy! The Express-like route definitions are easy to understand and work with.
            </p>
        </div>
        <div class="right">
            <p>
                <img src="https://i.imgur.com/vH926QI.png" />
            </p>
        </div>
    </div>
</section>

<section class="generic-block reverse faint">
    <div class="mid">
        <div class="left">
            <h3>Low Memory Footprint</h3>
            <p>
                Jiny's low memory footprint allows you to implement features without worrying too much about how much
                 memory your application will use. This allows you to focus on your application and its business logic, rather than technical particularities.
            </p>
        </div>
        <div class="right">
            <h3>
                Rapid Programming
            </h3>
            <p>
                Take your idea and turn it into reality in no time! Thanks to the well-designed and easy-to-learn API, you can develop your application in record speed (especially if you're coming from an Express.js background).
            </p>
        </div>
    </div>
</section>

<section class="generic-block reverse">
    <div class="mid">
        <div class="left">
            <h3>API-ready</h3>
            <p>
                Are you building an API server? We've got you covered! Jiny is the perfect choice for building REST
                 APIs in Java. Receiving and sending data is fast and easy!
            </p>
        </div>
        <div class="right">
            <p>
                <img src="https://i.imgur.com/Ch3EgNF.png" />
            </p>
        </div>
    </div>
</section>

<section class="generic-block faint">
    <div class="mid">
        <div class="left">
            <h3>Flexible Middleware Support</h3>
            <p>
                Choose from a number of already existing middleware or create your own! Use them to verify and manipulate certain requests in your app before they reach your controller.
            </p>
        </div>
        <div class="right">
            <p>
                <img src="https://i.imgur.com/0vUMBh6.png" />
            </p>
        </div>
    </div>
</section>

<section class="generic-block reverse">
    <div class="mid">
        <div class="left">
            <h3>Websocket Support</h3>
            <p>
                Use the power of asynchronous non-blocking WebSockets in your Jiny app! Build fast interactive user
                 experiences with
                 performance and scalability guaranteed.
            </p>
        </div>
        <div class="right">
            <p>
                <img src="https://i.imgur.com/zadW6vI.png" />
            </p>
        </div>
    </div>
</section>

<div style="max-width: 960px; margin: auto; text-align: center; font-size: 1.5rem; font-weight
: bold;">Who're
 using
 Jiny?</div>

<div style="text-align: center; margin-top: 1.5rem; margin-bottom: 2rem;">
<a href="https://oddgame.io" target="_blank"><img src="https://i.imgur.com/0JNoKJd.png" style="max-width: 92px; margin-right: 0.5rem;" /></a>
<a href="https://engineering.linecorp.com/en/opensource/" target="_blank"><img src="https://i.imgur.com/PfIIONx.png" style="max-width: 100px;" /></a>
</div>