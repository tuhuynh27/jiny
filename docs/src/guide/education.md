# For Educational

Jiny is well suited for programming courses and for demos/prototypes.

A lot of universities still use application servers such as Glassfish or Tomcat when teaching Java web development. Setting up and configuring these servers for each student requires a lot of effort, and that effort could be spent teaching students about HTTP and programming instead.

::: details Why use enterprise frameworks to learn web development is a bad idea?
**Enterprise** application servers such as [Glassfish](https://javaee.github.io/glassfish/) and [JBoss](https://www.jboss.org/) are monolithic and needlessly complex, providing a wealth of configuration options and services that are mostly dormant. More lightweight application servers like [Tomcat](http://tomcat.apache.org/) are easier to configure, but still have a learning curve to do so properly.

HTTP, in essence, is a very simple protocol. Each HTTP message has a preamble, some key-value pair headers, and a content body. All of the magic in a web application comes from how those messages are interpreted.
:::

Jiny was built on Java SE (yes, just Java's standard lib) thus you only need to add the dependency and write a single line of code to create and start a server. A full “Hello World” server looks like this:

:::: tabs

::: tab Java7
 ```java
import com.jinyframework.*;

class Main {
    // Syntax is very expressive, you don't need to read the docs to understand
    static void main(String[] args) {
        HttpServer server = HttpServer.port(1234); // Server will listen at port 1234

        // If a client visits on path '/'
        server.use("/", new Handler() {
            @Override
            public HttpResponse handleFunc(Context context) {
                // The server will send a response said: Hello World
                return HttpResponse.of("Hello World!");
            }
        });

        server.start(); // Start the server
    }
} 
```
:::

::: tab Java8
 ```java
import com.jinyframework.*;

class Main {
    // Syntax is very expressive, you don't need to read the docs to understand
    static void main(String[] args) {
        HttpServer server = HttpServer.port(1234); // Server will listen at port 1234

        // If a client visits on path '/', the server will send a response said: Hello World
        server.use("/", ctx -> HttpResponse.of("Hello World!"));

        server.start(); // Start the server
    }
} 
```
:::


::: tab Scala
```scala
import com.jinyframework.*

object Main extends App {
  val server = HttpServer.port(1234)
  server.get("/", ctx => HttpResponse.of("Hello World))
  server.start()
}
```
:::

::: tab Kotlin
```kotlin
import com.jinyframework.*

fun main(args: Array<String>) {
    val server: HttpServer = HttpServer.port(1234)
    server.get("/", ctx -> HttpResponse.of("Hello World"))
    server.start()
}
```
:::

::::

This server can be packaged and launched with `java -jar hello-world.jar`, no further configuration required. This lets you focus your classes on core principles rather than specifics for setting up an application server (and understand its complex "Enterprise" architecture).

## Simple and unopinionated

Jiny is just a couple of thousands lines of Java SE code. Essentially, there is no magic implemented under-the-hood, which makes it easy to reason about the program's logic flows.

- No annotations, no implicit state
- No global static state
- No reflection
- No configuration files
- No JavaEE, no Servlet, no Servlet container

Unlike restrictive traditional application frameworks, Jiny is **not a restrictive framework**, and thus doesn't force you a correct way to write a web application. Instead Jiny give you a lot of useful bricks and let you create your server the way you want to, with a minimum of fuss, in your favorite JVM language.

## Good documentations and tutorials

Jiny’s documentation is example-based rather than technical, which allows new users to copy snippets and experiment with them. Jiny also has examples for most common tasks that software engineers have to solve when starting server-side programming.

## Asynchronous programming support

Jiny standard mode is designed to be simple and blocking, as this is the easiest programming model to reason about. However, you can still switch to use with Future, ComplatableFuture or Reactive Programming since Jiny also support asynchronous mode out-of-the-box.

:::tip What is Async Mode?
[See NIO APIs](https://jinyframework.com/guide/nio/)
:::

Jiny Async mode is event-driven and non-blocking. This means your server can handle a lot of concurrency using a small number of kernel threads, thus help your app scale with minimal hardware.

## Interoperable

Other Java frameworks usually offer separate version for each JVM language. Jiny is being developed with interoperability in mind, so servers are built the same way in every JVM languages: Java, Kotlin, Clojure or Scala.
