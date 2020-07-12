# Lightweight Java HTTP Server

A bottom-up approach to learn the network programming in Java core, by creating my own server library.

![](https://miro.medium.com/max/1400/1*ziPHz443ne9yNwK0CmA0lQ.png)

## Why?

I build this for my [LINE Bot webhook server](https://github.com/huynhminhtufu/line-bot) which will be rewritten in Java, [Servlet APIs](https://docs.oracle.com/javaee/7/api/javax/servlet/package-summary.html) / [JavaEE](https://www.oracle.com/java/technologies/java-ee-glance.html) stuff is too heavy-weight, super complex and f*cking verbose and Java 8 SE is lacking a built-in simple HTTP handler. :smirk:

## How to use com.tuhuynh.httpserver

(thanks [Github Packages](https://github.com/huynhminhtufu/httpserver/packages/309436) <3)

1. Add this to pom.xml:

```xml
<dependency>
  <groupId>com.tuhuynh</groupId>
  <artifactId>httpserver</artifactId>
  <version>0.1.4-ALPHA</version>
</dependency>
```

or build.gradle:

```groovy
compile group: 'com.tuhuynh', name: 'httpserver', version: '0.1.4-ALPHA'
```

2. Use it (Java 8 compatible!)

(It's easy to use like a Node.js [Express](https://expressjs.com/) server or Golang's built-in [net/http](https://golang.org/pkg/net/http/) package)

Note: This HTTP Server library is inspired by **[LINE's Armeria](https://armeria.dev/)**

```java
public final class TestServer {
    public static void main(String[] args) throws IOException {
        final HTTPServer server = new HTTPServer(8080);

        server.addHandler(RequestMethod.GET, "/", ctx -> HttpResponse.of("Hello World"));
        server.addHandler(RequestMethod.POST, "/echo", ctx -> HttpResponse.of(ctx.getPayload()));

        // Free to execute blocking tasks with a Cached ThreadPool
        server.addHandler(RequestMethod.GET, "/sleep", ctx -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            return HttpResponse.of("Sleep done!");
        });

        server.addHandler(RequestMethod.GET, "/thread",
                          ctx -> HttpResponse.of(Thread.currentThread().getName()));

        server.addHandler(RequestMethod.GET, "/random", ctx -> {
            final Random rand = new Random();
            return HttpResponse.of(String.valueOf(rand.nextInt(100 + 1)));
        });

        // Get query params, ex: /query?hello=world
        server.addHandler(RequestMethod.GET, "/query", ctx -> {
            final String world = ctx.getQueryParams().get("hello");
            return HttpResponse.of("Hello: " + world);
        });

        // Middleware support: Sample JWT Middleware
        server.addHandler(RequestMethod.GET, "/protected", ctx -> {
            final String authorizationHeader = ctx.getHeader().get("Authorization");
            // Check JWT is valid, below is just a sample check
            if (!authorizationHeader.startsWith("Bearer")) {
                return HttpResponse.reject("Invalid token").status(401);
            }

            ctx.putHandlerData("username", "tuhuynh");
            return HttpResponse.next();
        }, ctx -> HttpResponse.of("Login success, hello: " + ctx.getHandlerData("username")));

        // Perform as a proxy server
        server.addHandler(RequestMethod.GET, "/meme", ctx -> {
            // Built-in HTTP Client
            final ResponseObject
                    meme = HTTPClient.builder()
                                     .url("https://meme-api.herokuapp.com/gimme").method("GET")
                                     .build().perform();
            return HttpResponse.of(meme.getBody());
        });

        // Handle error
        server.addHandler(RequestMethod.GET, "/panic", ctx -> {
            throw new Exception("Panicked!");
        });

        server.start();
    }
}
```

(after build: size is just 30-50 KB .jar file, run and init cost about 20-30MB RAM)

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

### Up coming:

- Improve the request context with extra routing pattern matching (path: /request/:path)
- Support annotation to decorate the code (@Handler @Router)
- **Support an NIO (Non Blocking I/O) HTTP Server & Client based on `java.nio`**
- Support built-in JSON marshall/unmarshall support

## Dependencies

**Zero dependency**, it just uses the Java core built-in APIs, also `lombok` is used to compile and build the library.

Lines of code: 428
```
  39 ./src/main/java/com/tuhuynh/httpserver/HTTPServer.java
 140 ./src/main/java/com/tuhuynh/httpserver/utils/HandlerUtils.java
  74 ./src/main/java/com/tuhuynh/httpserver/HTTPClient.java
  61 ./src/main/java/com/tuhuynh/httpserver/handlers/HandlerPipeline.java
 101 ./src/main/java/com/tuhuynh/httpserver/handlers/HandlerBinder.java
 428 total
```