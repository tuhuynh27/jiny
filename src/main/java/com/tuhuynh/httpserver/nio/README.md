# Lightweight Java NIO HTTP Server

"Asynchronous" Non-blocking I/O version on `com.tuhuynh.httpserver`

## Quick Start

Install [com.tuhuynh.httpserver](https://github.com/huynhminhtufu/httpserver/packages/309436)

```java
public final class MiniServer {
    public static void main(String[] args) throws IOException {
        val server = NIOHTTPServer.port(1234);
        server.use("/", ctx -> CompletableFuture.completedFuture(HttpResponse.of("Hello World")));
        server.start(); // Listen and serve on localhost:1234
    }
}
```

## API Examples (use with CompletableFuture)

```java
public final class TestNIOServer {
    public static void main(String[] args) throws Exception {
        val workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        val server = NIOHTTPServer.port(1234);

        server.use("/", ctx -> CompletableFuture.completedFuture(HttpResponse.of("Hello World")));

        server.get("/thread", ctx -> CompletableFuture
                .completedFuture(HttpResponse.of(Thread.currentThread().getName())));

        // This request will not block the main thread (event loop)
        server.get("/sleep", ctx -> {
            CompletableFuture<HttpResponse> completableFuture = new CompletableFuture<>();

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
                completableFuture.complete(HttpResponse.of("Work has done, current thread is: " + thread));
            });

            return HttpResponse.promise(completableFuture);
        });

        // This request will block the main thread (event loop)
        server.use("/block", ctx -> {
            Thread.sleep(3000);
            return CompletableFuture.completedFuture(HttpResponse.of("Hello World"));
        });

        server.start();
    }
}
```
