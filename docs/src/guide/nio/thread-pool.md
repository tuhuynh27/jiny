# Worker ThreadPool

How to use ThreadPool to execute CPU-intensive task in NIO mode

# Block the EventLoop (Don't)

This request will block one of the event loop threads, by default you have cpu.length * 2 event loop threads

```java
server.use("/block", ctx -> {
    System.out.println(Thread.currentThread().getName() + " is gonna be blocked now!");
    Thread.sleep(60 * 1000); // Block for 60s
    return HttpResponse.ofAsync("Block one event loop thread!");
});
```

## Create WorkerPool

First you have to create a worker pool

```java
val workerPool = Executors
    .newScheduledThreadPool(Runtime.getRuntime()
                                    .availableProcessors() * 2);
```

## Execute in worker pool

This request will not block the main thread (event loop), It moves the blocking operation into another thread pool (`workerPool`):

```java
server.get("/sleep", ctx -> {
    val async = AsyncHelper.make();
    val eventLoopThread = Thread.currentThread().getName();

    workerPool.submit(() -> {
        System.out.println(ctx.getPath());
        val thread = Thread.currentThread().getName();
        System.out.println("Executing an expensive task on " + thread);
        Thread.sleep(5000);
        async.resolve("Work has done, current thread is: " + eventLoopThread);
    });

    return async.submit();
});
```
