# Lightweight Java HTTP Server

A bottom-up approach to learn the network programming in Java core, by creating my own server library.

![](https://miro.medium.com/max/1400/1*ziPHz443ne9yNwK0CmA0lQ.png)

## How to use com.tuhuynh.httpserver

1. Add this to pom.xml or build.gradle:

```xml
<dependency>
  <groupId>com.tuhuynh</groupId>
  <artifactId>httpserver</artifactId>
  <version>0.1-ALPHA</version>
</dependency>
```

```groovy
compile group: 'com.tuhuynh', name: 'httpserver', version: '0.1-ALPHA'
```

2. Use it:

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
        // Provide the server port
        final HTTPServer server = new HTTPServer(8080);
        
        server.addHandler(RequestMethod.GET, "/", ctx -> "Hello World");
        server.addHandler(RequestMethod.POST, "/echo", RequestContext::getPayload);
        
        server.addHandler(RequestMethod.GET, "/sleep", ctx -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            return "Done!";
        });
        server.addHandler(RequestMethod.GET, "/thread", ctx -> Thread.currentThread().getName());
        
        server.addHandler(RequestMethod.GET, "/random", ctx -> {
            final Random rand = new Random();
            return String.valueOf(rand.nextInt(100 + 1));
        });
        
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

### Up coming:

- Improve the request context with extra routing pattern matching (path: /request/:path)
- Support query params in request context
- Support annotation to beautify the code (@Handler)
- Support an NIO (Non Blocking I/O) HTTP Server based on `java.nio`