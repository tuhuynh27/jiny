# Error Handler

Just throw the error, Jiny will handle it for you:

```java
// Handle error
server.get("/panic", ctx -> {
    throw new RuntimeException("Panicked!");
});
```
