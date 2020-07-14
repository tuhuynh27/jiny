# Lightweight Java NIO HTTP Server

"Asynchronous" Non-blocking I/O version on `com.tuhuynh.httpserver`, using Java SE's `java.net` & `java.nio` packages.

This is kinda naive "[Netty](https://netty.io/) clone with [Go-Gin](https://github.com/gin-gonic/gin) interface" but in HTTP level (Netty is just TCP framework).  

**Currently, this is in experimental mode**

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
        val workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        val server = NIOHTTPServer.port(1234);
        
        // Global middleware
        server.use(ctx -> {
            System.out.println("This is a global middleware");

            return HttpResponse.nextAsync();
        });


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

        // This request will block the main thread (event loop)
        server.use("/block", ctx -> {
            Thread.sleep(10 * 1000);
            return HttpResponse.ofAsync("Block the event loop!");
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