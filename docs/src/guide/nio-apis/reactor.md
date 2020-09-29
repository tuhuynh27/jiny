# Reactor

You can also use NIO mode with Reactive Streams systems such as Reactor or RxJava:

```java
server.get("/", ctx -> Mono.fromCallable(System::currentTimeMillis)
                           .map(HttpResponse::of)
                           .toFuture());

// Instead of using CompletableFuture, we can use Reactor's Mono here:
server.get("/blocking", ctx ->
        Mono.just("blocking")
            .subscribeOn(Schedulers.boundedElastic()) 
            // Execute it on another thread pool
            .map(text -> {
                // Blocking operation
                Thread.sleep(1000);
                return text;
            })
            .map(HttpResponse::of)
            .toFuture());

server.start();
```

Or you can use EA's async-await to wrap the async code, improve the readability.
