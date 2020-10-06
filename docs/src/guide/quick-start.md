# Quick Start

You have your REST API ready to serve JSON in less than ten lines of code

:::: tabs

::: tab Java
 ```java
import com.jinyframework.*;
import com.google.gson.Gson;

public class Main {
    public static void main(String args[]) {
        val server = HTTPServer.port(1234);
        val gson = new Gson();
        server.useTransformer(gson::toJson);
        server.get("/ping", ctx -> HttpResponse.of("Pong"));
        server.start();
    }
} 
```
:::


::: tab Scala
```scala
import com.jinyframework.*
import your.server.factories.app.AppFactory

object Main extends App {
  val server = HttpServer.port(1234)
  server.useTransformer(s => AppFactory.getGson.toJson(s))
  server.get("/ping", ctx => HttpResponse.of("Pong))
  server.start()
}
```
:::

::: tab Kotlin
```kotlin
import com.jinyframework.*
import com.google.gson.Gson;

fun main(args: Array<String>) {
    val server: HttpServer = HTTPServer.port(1234)
    val gson = new Gson()
    server.useTransformer(gson::toJson);
    server.get("/ping", ctx -> HttpResponse.of("Pong"))
    server.start()
}
```
:::

::::
