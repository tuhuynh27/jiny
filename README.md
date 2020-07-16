# Lightweight Java HTTP Server

`com.tuhuynh.httpserver` is a light-weight (tiny) HTTP Server (handler/router) written in Vanilla Java with no dependency (based on `java.net` & `java.io` packages). It features a simple HTTP Handler including request parser, routing, middlewares and more. If you need a quick start & simple way to write a Java server, you will love this library.

## Why?

I build this for my [LINE Bot webhook server](https://github.com/huynhminhtufu/line-bot) which will be rewritten in Java, [Servlet APIs](https://docs.oracle.com/javaee/7/api/javax/servlet/package-summary.html) / [JavaEE](https://www.oracle.com/java/technologies/java-ee-glance.html) stuff is too heavy-weight (The Servlet APIs require that your application must be run within a servlet container), super complex and very verbose, also Java 8 SE is lacking a built-in simple HTTP handler/router.

There seems to be this perception that Java in itself is lacking some features that facilitate proper application development (unlike like many feature of [Go](https://golang.org/pkg/net/http/)).

**Experimental: [Non-blocking Asynchronous HTTP Server](https://github.com/huynhminhtufu/httpserver/tree/master/src/main/java/com/tuhuynh/httpserver/core/nio)** (if you have a large RPS want to save your server's resources)

## Installation

(thanks [Github Packages](https://github.com/huynhminhtufu/httpserver/packages/309436) <3)

Add this to `pom.xml`:

```xml
<dependency>
  <groupId>com.tuhuynh</groupId>
  <artifactId>httpserver</artifactId>
  <version>0.1.7-ALPHA</version>
</dependency>
```

or `build.gradle`:

```groovy
compile group: 'com.tuhuynh', name: 'httpserver', version: '0.1.7-ALPHA'
```

## Quick Start

```java
public final class MiniServer {
    public static void main(String[] args) throws IOException {
        val server = HTTPServer.port(1234);
        server.get("/ping", ctx -> HttpResponse.of("Pong"));
        server.start(); // Listen and serve on localhost:1234
    }
}
```

## API Examples

It's very easy to use just like [Go Gin](https://github.com/gin-gonic/gin) or Golang's built-in [net/http](https://golang.org/pkg/net/http/) package as it has similar APIs.

```java
import com.tuhuynh.httpserver.HttpClient;
import com.tuhuynh.httpserver.HttpServer;
import com.tuhuynh.httpserver.core.RequestBinder.HttpResponse;

val server = HttpServer.port(1234);

server.use("/", ctx -> HttpResponse.of("Hello World"));
server.post("/echo", ctx -> HttpResponse.of(ctx.getBody()));

// Free to execute blocking tasks with a Cached ThreadPool
server.get("/sleep", ctx -> {
    try {
        Thread.sleep(5000);
    } catch (InterruptedException e) {
        System.out.println(e.getMessage());
    }
    return HttpResponse.of("Sleep done!");
});

server.get("/thread",
           ctx -> HttpResponse.of(Thread.currentThread().getName()));

server.get("/random", ctx -> {
    val rand = new Random();
    return HttpResponse.of(String.valueOf(rand.nextInt(100 + 1)));
});

// You can put in a JSON transform adapter like Google GSON
server.get("/json", ctx -> {
    val customer = CustomObject.builder()
                               .email("abc@gmail.com")
                               .name("Tu Huynh").build();
    val gson = new Gson();
    return HttpResponse.of(customer)
                       .transform(gson::toJson);
    // You will get {"email": "abc@gmail.com", "name": "Tu Huynh"}
});

// Get query params, ex: /query?hello=world
server.get("/query", ctx -> {
    val world = ctx.getQuery().get("hello");
    return HttpResponse.of("Hello: " + world);
});

// Get handler params, ex: /params/:categoryID/:itemID
server.get("/params/:categoryID/:itemID", ctx -> {
    val categoryID = ctx.getParam().get("categoryID");
    val itemID = ctx.getParam().get("itemID");
    return HttpResponse.of("Category ID is " + categoryID + ", Item ID is " + itemID);
});

// Catch all
server.get("/all/**", ctx -> HttpResponse.of(ctx.getPath()));

// Middleware support
server.get("/protected", // You wanna provide a jwt validator on this endpoint
           ctx -> {
               val authorizationHeader = ctx.getHeader().get("Authorization");
               // Check JWT is valid, below is just a sample check
               if (!authorizationHeader.startsWith("Bearer")) {
                   return HttpResponse.reject("Invalid token").status(401);
               }
               ctx.putHandlerData("username", "tuhuynh");
               return HttpResponse.next();
           }, // Injected
           ctx -> HttpResponse.of("Login success, hello: " + ctx.getData("username")));

// Global middleware
server.use(ctx -> {
    val thread = Thread.currentThread().getName();
    System.out.println("Serving in " + thread);
    return HttpResponse.next();
});

// Perform as a proxy server
server.get("/meme", ctx -> {
    // Built-in HTTP Client
    val meme = HttpClient.builder()
                         .url("https://meme-api.herokuapp.com/gimme")
                         .method("GET")
                         .build().perform();
    return HttpResponse.of(meme.getBody())
                       .status(meme.getStatus());
});

// Handle error
server.get("/panic", ctx -> {
    throw new RuntimeException("Panicked!");
});

server.start();
```

(after build: size is just 30-50 KB .jar file, run and init cost about ~20MB RAM)

## Changelogs

### 0.1.0-ALPHA

- A very naive and basic HTTP Server
- Raw implementation, lightweight & no dependency
- Listen on a given TCP Port
- Easy to add a handler with a (Method/Path/Functional_Handler) define
- Handled inside with a Cached ThreadPool

### 0.1.1-ALPHA

- Add HttpResponse Object for handling response struct
- Remove redundant constant and lombok usage

### 0.1.2-ALPHA

- Add built-in HTTP Client
- Refactor code

### 0.1.3-ALPHA

- Support default error handling
- Support get query params from Context
- Improve HTTP Client response

### 0.1.4-ALPHA

- Support HTTP Middleware functions chain (like Node.js Express and Go)

### 0.1.5-ALPHA

- Support routing handler params, ex: /params/:categoryID/:itemID
- Support request path's slash trim
- Refactor code

### 0.1.6-ALPHA

- Added an experimental NIO Server
- Refactor code

### 0.1.7-ALPHA

- Update NIO Server to use with `AsynchronousServerSocketChannel` API
- Fix some bugs in routing handlers (duplicate middleware)
- Refactor code

### Up coming:

- Support CORS config, body compression and some default middlewares
- Improve matching/routing performance by using [dynamic trie](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.12.7321&rep=rep1&type=pdf) (radix tree) structure
- Support annotation to decorate the code (@Handler @Router)

## Dependencies

**Zero dependency**, it just uses the Java core built-in APIs, also `lombok` is used to compile and build the library.

Lines of code: **1163**
```
  64 ./src/main/java/com/tuhuynh/httpserver/core/bio/RequestPipeline.java
  62 ./src/main/java/com/tuhuynh/httpserver/core/bio/RequestBinder.java
  35 ./src/main/java/com/tuhuynh/httpserver/core/ServerThreadFactory.java
 191 ./src/main/java/com/tuhuynh/httpserver/core/RequestBinder.java
 112 ./src/main/java/com/tuhuynh/httpserver/core/nio/RequestPipeline.java
  85 ./src/main/java/com/tuhuynh/httpserver/core/nio/RequestBinder.java
  34 ./src/main/java/com/tuhuynh/httpserver/core/nio/AsyncHelper.java
 136 ./src/main/java/com/tuhuynh/httpserver/core/RequestParser.java
  14 ./src/main/java/com/tuhuynh/httpserver/tests/TestClient.java
  87 ./src/main/java/com/tuhuynh/httpserver/tests/TestNIOServer.java
  94 ./src/main/java/com/tuhuynh/httpserver/tests/TestServer.java
  81 ./src/main/java/com/tuhuynh/httpserver/HttpServer.java
  94 ./src/main/java/com/tuhuynh/httpserver/NIOHttpServer.java
  74 ./src/main/java/com/tuhuynh/httpserver/HttpClient.java
1163 total
```

## Side project

A bottom-up approach to learn the network programming in Java core, by creating my own server library.

![](https://miro.medium.com/max/1400/1*ziPHz443ne9yNwK0CmA0lQ.png)