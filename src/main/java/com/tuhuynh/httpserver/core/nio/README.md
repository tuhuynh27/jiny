# Lightweight Java Async NIO HTTP Server

"Asynchronous" Non-blocking I/O version on `com.tuhuynh.httpserver`, using Java SE's `java.net` & `java.nio` packages.

This is kinda "naive [Netty](https://netty.io/) clone with [Go-Gin](https://github.com/gin-gonic/gin) interface" but with HTTP protocol support (Netty is a TCP Framework).  

**Currently, this is in the experimental mode**

### Insight

This server uses the latest `AsynchronousServerSocketChannel` API of Java SE 7, which is fully Async I/O (with underlying OS support for `epoll`/`kqueue` edge-triggered syscalls).

The [old versions](https://github.com/huynhminhtufu/httpserver/blob/678bc216a91d8d6504983c7cd22d1c1cef1e88bd/src/main/java/com/tuhuynh/httpserver/core/nio/RequestPipelineNIO.java) (< 0.1.6) use `SocketServerChannel` and a `Selector` which is just a NIO Multiplexer aka "polling mechanism" (with `select` and `poll` which is lever-triggered)

## Quick Start

Install [com.tuhuynh.httpserver](https://github.com/huynhminhtufu/httpserver/packages/309436)

This NIO HTTP Server is fully compatible with [HTTPServer's API](https://github.com/huynhminhtufu/httpserver#api-examples)

```java
public final class MiniServer {
    public static void main(String[] args) throws IOException {
        val server = NIOHTTPServer.port(1234);
        server.use("/", ctx -> HttpResponse.ofAsync("Hello World"));
        server.start(); // Listen and serve on localhost:1234
    }
}
```

## API Examples (use with `CompletableFuture`)

```java
public final class TestNIOServer {
    public static void main(String[] args) throws Exception {
        val workerPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        val server = NIOHTTPServer.port(1234);

        server.use("/", ctx -> HttpResponse.ofAsync("Hello World"));

        server.get("/thread", ctx -> HttpResponse.ofAsync(Thread.currentThread().getName()));

        // This request will not block the main thread (event loop)
        server.get("/sleep", ctx -> {
            val async = AsyncHelper.make();

            workerPool.submit(() -> {
                try {
                    // Some expensive / blocking task
                    val thread = Thread.currentThread().getName();
                    System.out.println("Executing an expensive task on " + thread);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }

                val thread = Thread.currentThread().getName();
                async.resolve("Work has done, current thread is: " + thread);
            });

            return async.submit();
        });

        // This request will block one of the event loop thread
        // By default you have cpu.length * 2 event loop thread
        server.use("/block", ctx -> {
            System.out.println(Thread.currentThread().getName() + " is gonna be blocked now!");
            Thread.sleep(60 * 1000); // Block for 60s
            return HttpResponse.ofAsync("Block one event loop thread!");
        });

        // Middleware
        server.get("/protected", ctx -> {
            val authorizationHeader = ctx.getHeader().get("Authorization");
            if (!authorizationHeader.startsWith("Bearer ")) {
                return HttpResponse.rejectAsync("InvalidToken", 401);
            }
            ctx.putHandlerData("username", "tuhuynh");
            return HttpResponse.nextAsync();
        }, ctx -> HttpResponse.ofAsync("Login success, hello: " + ctx.getData("username")));

        server.start();
    }
}
```

But I think it's better to use [async-await](https://github.com/electronicarts/ea-async) to wrap the async code, improve the readability.
