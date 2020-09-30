# For Educational

Jiny is well suited for programming courses and for demos/prototypes.

A lot of universities still use application servers such as Glassfish or Tomcat when teaching Java web development. Setting up and configuring these servers for each student requires a lot of effort, and that effort could be spent teaching students about HTTP and programming instead.

Jiny was built on Java SE (yes, just Java's standard lib) thus you only need to add the dependency and write a single line of code to create and start a server. A full “Hello World” server looks like this:

:::: tabs

::: tab Java
 ```java
import com.jinyframework.*;

class Main {
    static void main() {
        val server = HTTPServer.port(1234);
        server.get("/", ctx -> HttpResponse.of("Hello World"));
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
  server.get("/", ctx => HttpResponse.of("Hello World))
  server.start()
}
```
:::

::: tab Kotlin
```kotlin
import com.jinyframework.*

fun main(args: Array<String>) {
    val server: HttpServer = HTTPServer.port(1234)
    server.get("/", ctx -> HttpResponse.of("Hello World"))
    server.start()
}
```
:::

::::

This server can be packaged and launched with java -jar hello-world.jar, no further configuration required. This lets you focus your classes on core principles rather than specifics for setting up an application server (and understand its complex "Enterprise" architecture).

## Simple and unopinionated

Jiny is just a couple of thousands lines of Java SE code. Essentially, there is no magic implemented under-the-hood, which makes it easy to reason about the program's logic flows.

- No annotations, no implicit
- No global static state
- No reflection
- No configuration files
- No JavaEE, no Servlet, no Servlet container

Jiny does not care how you build your app, so any knowledge obtained while working with a Jiny project should transfer easily to other (non-Jiny) projects.

## Good documentations and tutorials

Jiny’s documentation is example-based rather than technical, which allows new users to copy snippets and experiment with them. Jiny also has examples for most common tasks that software engineers have to solve when starting web-programming.

## Asynchronous programming support

Jiny standard mode is designed to be simple and blocking, as this is the easiest programming model to reason about. However, you can still switch to use with Future, ComplatableFuture or Reactive Programming since Jiny also support asynchronous mode out-of-the-box.

:::tip What is Async Mode?
[See NIO APIs](http://localhost:8080/guide/nio-apis)
:::

## Interoperable

Other Java frameworks usually offer separate version for each JVM language. Jiny is being developed with interoperability in mind, so servers are built the same way in every JVM languages: Java, Kotlin or Scala.
