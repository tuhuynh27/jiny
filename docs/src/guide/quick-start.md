# Quick Start

 You have your REST API ready to serve JSON in less than ten lines of code:
 
 Java
 
 ```java
import com.jinyframework.*;

class Main {
    static void main(String[] args) throws IOException {
        val server = HTTPServer.port(1234);
        server.get("/ping", ctx -> HttpResponse.of("Pong"));
        server.start(); // Listen and serve on localhost:1234
    }
} 
```

Scala

```scala
import com.jinyframework.*

object Main extends App {
  val server = HttpServer.port(1234)
  server.get("/**", ctx => HttpResponse.of(ctx.getPath))
  server.start()
}
```

Kotlin

```kotlin
import com.jinyframework.*

fun main(args: Array<String>) {
    val server: HttpServer = HTTPServer.port(1234)
    server.get("/ping", ctx -> HttpResponse.of("Pong"))
    server.start() // Listen and serve on localhost:1234
}
```
