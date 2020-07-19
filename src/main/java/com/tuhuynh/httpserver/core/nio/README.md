# Lightweight Java Async NIO HTTP Server

"Asynchronous" Non-blocking I/O version on `com.tuhuynh.httpserver`, using Java SE's `java.net` & `java.nio` packages.

This is kinda "naive [Netty](https://netty.io/) clone with [Go-Gin](https://github.com/gin-gonic/gin) interface" also with an easy-to-use API interface covering HTTP protocol support (Netty is just a TCP Framework).  

**Currently, this is in the experimental mode**

### NIO Insight

This server uses the latest `AsynchronousServerSocketChannel` (aka NIO.2 AIO API) API of Java SE 7, which use the [Proactor Pattern](https://en.wikipedia.org/wiki/Proactor_pattern) and it is fully Async I/O (with underlying OS support for `epoll`/`kqueue` edge-triggered syscalls).

The [old versions](https://github.com/huynhminhtufu/httpserver/blob/678bc216a91d8d6504983c7cd22d1c1cef1e88bd/src/main/java/com/tuhuynh/httpserver/core/nio/RequestPipelineNIO.java) (< 0.1.6) use `SocketServerChannel` and a `Selector` (aka NIO API) which is just a I/O Multiplexer aka "polling mechanism" (with `epoll` / `kqueue` level-triggered syscalls).

## Quick Start

Install [com.tuhuynh.httpserver](https://github.com/huynhminhtufu/httpserver/packages/309436)

This NIO HTTP Server is fully compatible with [Lightweight HTTPServer's API](https://github.com/huynhminhtufu/httpserver#api-examples)

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
import com.tuhuynh.httpserver.NIOHttpServer;
import com.tuhuynh.httpserver.core.RequestBinder.HttpResponse;
import com.tuhuynh.httpserver.core.nio.AsyncHelper;

val workerPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
val server = NIOHttpServer.port(1234);

// Similar with HTTP BIO Server's API, but you need to include the "Async" at the end of the name
server.use("/", ctx -> HttpResponse.ofAsync("Hello World"));

server.post("/echo", ctx -> HttpResponse.ofAsync(ctx.getBody()));

server.get("/thread", ctx -> HttpResponse.ofAsync(Thread.currentThread().getName()));

// You can put in a JSON transform adapter like Google GSON
server.get("/json", ctx -> {
    val customer = CustomObject.builder()
                               .email("abc@gmail.com")
                               .name("Tu Huynh").build();
    val gson = new Gson();
    return HttpResponse.ofAsync(customer, gson::toJson);
    // You will get {"email": "abc@gmail.com", "name": "Tu Huynh"}
});

// /query?foo=bar
server.get("/query", ctx -> {
    final String bar = ctx.getQuery().get("foo");
    return HttpResponse.ofAsync(bar);
});

// /params/hello/world
server.get("/params/:foo/:bar", ctx -> {
    final String foo = ctx.getParam().get("foo");
    final String bar = ctx.getParam().get("bar");
    return HttpResponse.ofAsync("Foo: " + foo + ", Bar: " + bar);
});

// Catch all
server.get("/all/**", ctx -> HttpResponse.ofAsync(ctx.getPath()));

// This request will not block the main thread (event loop)
// It move the blocking operation into another thread pool (workerPool)
server.get("/sleep", ctx -> {
    val async = AsyncHelper.make();

    workerPool.submit(() -> {
        System.out.println(ctx.getPath());
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

// This request will block one of the event loop threads
// By default you have cpu.length * 2 event loop threads
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

// Handle error
server.get("/panic", ctx -> {
    throw new Exception("Panicked!");
});

server.start();
```

## Or use with Reactive Streams systems such as `Reactor` (or `RxJava`):

```java
server.get("/", ctx -> Mono.fromCallable(System::currentTimeMillis)
                           .map(HttpResponse::of)
                           .toFuture());

server.get("/blocking", ctx ->
        Mono.just("blocking")
            .subscribeOn(Schedulers.boundedElastic()) // Execute it on another thread pool
            .map(text -> {
                // Blocking operation
                Thread.sleep(1000);
                return text;
            })
            .map(HttpResponse::of)
            .toFuture());

server.start();
```

(after build: size is just 30-50 KB .jar file, run and init cost about ~20MB RAM compared to ~20MB.jar and 300MB RAM init of [Spring Boot WebFlux Netty](https://start.spring.io/))

Or you can use [async-await](https://github.com/electronicarts/ea-async) to wrap the async code, improve the readability.
