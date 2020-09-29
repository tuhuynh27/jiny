# Async Helper

## Create an Async Object

You can import and create an Async Object

```java
import com.jinyframework.core.nio.AsyncHelper;

val async = AsyncHelper.make();
```

## Wrap the callbacks

It's better to wrap the callback by Async Object

```java
val async = AsyncHelper.make();

asyncCall(result -> {
    // Result callback
    async.resolve(result); // Return CompleableFuture<HttpResponse<T>>
}, err -> {
    // Error callback
    async.reject(err);
});

return async.submit() // Return the CompletableFuture object
```
