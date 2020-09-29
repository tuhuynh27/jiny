# CompletableFuture

API for Java's CompletableFuture

The NIO API is very similar with BIO API:

```java
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
