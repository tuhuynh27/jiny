# HttpResponse

You can use the `HttpResponse` Object to handle responses in handlers

## Normal Response

This will respond with HTTP status 200 with body content: `Hello World`:

```java
HttpResponse.of("Hello World!");
```

## Custom HTTP Response Code

This will respond with HTTP status 404 with body content: `Not found`:

```java
HttpResponse.of("Not found").status(404);
```
