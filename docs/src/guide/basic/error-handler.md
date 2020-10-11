# Error Handler

Route handlers can either throw an error or return a failed Future (if in NIO mode). Throwing or returning a error will result in a 500 status response and the error will be logged. 

Just throw the error, Jiny will handle it for you:

```java
// Handle error
server.get("/panic", ctx -> {
    throw new RuntimeException("Panicked!");
});
```

Then when a client hit `/panic`, Jiny server will respond:
```text
HTTP/1.1 500 Internal Server Error
Content-Type: text/plain; charset=utf-8
Content-Length: 8
Date: Fri, 04 Aug 2020 03:51:31 GMT

Panicked
```
