# HttpResponse

You can use the `HttpResponse` Object to handle responses in handlers

## Normal Response

This will respond with HTTP status 200:

```java
HttpResponse.of("Hello World!");
```

## Custom HTTP Response Code

This will respond with HTTP status 404:

```java
HttpResponse.of("Post").status(404);
```
