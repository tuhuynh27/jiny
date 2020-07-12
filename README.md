# Lightweight Java HTTP Server

A bottom-up approach to learn the network programming in Java core, by creating my own server library.

![](https://miro.medium.com/max/1400/1*ziPHz443ne9yNwK0CmA0lQ.png)

## How to use com.tuhuynh.httpserver

1. Add this to pom.xml:

```xml
<dependency>
  <groupId>com.tuhuynh</groupId>
  <artifactId>httpserver</artifactId>
  <version>0.1.1-ALPHA</version>
</dependency>
```

or build.gradle:

```groovy
compile group: 'com.tuhuynh', name: 'httpserver', version: '0.1.1-ALPHA'
```

2. Use it:

Note: This HTTP Server library is inspired by **[LINE's Armeria](https://armeria.dev/)**

(It's easy to use like a Node.js Express server)

```java
package com.server.test;

import java.io.IOException;
import java.util.Random;

import com.tuhuynh.httpserver.HTTPServer;
import com.tuhuynh.httpserver.utils.HandlerUtils.RequestContext;
import com.tuhuynh.httpserver.utils.HandlerUtils.RequestMethod;

public final class Main {
    public static void main(String[] args) throws IOException {
        final HTTPServer server = new HTTPServer(8080);

        server.addHandler(RequestMethod.GET, "/", ctx -> HttpResponse.of("Hello World"));
        server.addHandler(RequestMethod.POST, "/echo", ctx -> HttpResponse.of(ctx.getPayload()));

        server.addHandler(RequestMethod.GET, "/sleep", ctx -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            return HttpResponse.of("Sleep done!");
        });
        server.addHandler(RequestMethod.GET, "/thread", ctx -> HttpResponse.of(Thread.currentThread().getName()));

        server.addHandler(RequestMethod.GET, "/random", ctx -> {
            final Random rand = new Random();
            return HttpResponse.of(String.valueOf(rand.nextInt(100 + 1)));
        });

        server.addHandler(RequestMethod.GET, "/panic", ctx -> HttpResponse.of("Panic").status(500));

        server.start();
    }
}
```

(after build: 15kb .jar file)

## Features

### 0.1.0-ALPHA

- A very naive and basic HTTP Server
- Listen on a given TCP Port
- Easy to add a handler with a (Method/Path/Functional_Handler) define
- Handled inside with a Cached ThreadPool

### 0.1.1-ALPHA

- Add HttpResponse Object for handling response struct
- Remove redundant constant and lombok usage

### Up coming:

- Improve the request context with extra routing pattern matching (path: /request/:path)
- Support query params in request context
- Support annotation to beautify the code (@Handler)
- Support an NIO (Non Blocking I/O) HTTP Server based on `java.nio`

## Dependencies

**Zero dependency**, it just use the Java core built-in APIs, `lombok` is used to compile and build the library.