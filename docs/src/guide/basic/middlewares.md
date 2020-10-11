# Middlewares

Middleware is a logic chain between the client and the route handler. It allows you to perform operations on incoming requests before they get to the route handler and on outgoing responses before they go to the client.

## Definition

Middleware can be defined by `Handler Chaining`

You can pass multiple handlers per route define, the below example is a JWT middleware:

```java
server.get("/protected", // You wanna provide a jwt validator on this endpoint
           ctx -> {
               val authorizationHeader = ctx.getHeader().get("Authorization");
               // Check JWT is valid, below is just a sample check
               if (!authorizationHeader.startsWith("Bearer")) {
                   return HttpResponse.reject("Invalid token").status(401);
               }
               ctx.putHandlerData("username", "tuhuynh");
               return HttpResponse.next();
           }, // Injected
           ctx -> 
               HttpResponse.of("Login success, hello: " + 
                                                   ctx.getData("username")));
```

::: tip Notice the order
The order in which middleware are added is important. Requests coming into your application will go through the middleware in the order they are added. Responses leaving your application will go back through the middleware in reverse order. Route-specific middleware always runs after application middleware.
:::

## Handler Data

You can use `ctx.setDataParam('key', 'value')` to set HandlerData, this data can be access by the next middleware handler

```java
ctx.setDataParam("username", "tuhuynh"); // In JWT Middleware
ctx.getData("username") // After the JWT Middleware
```

## Next/Reject

In middleware handlers, use can use `HttpResponse.next()` and `HttpResponse.reject()`

```java
HttpResponse.next(); // Next to next handler

// or

HttpResponse.reject("Invalid token").status(401); // Reject and return error
```

## Functions

You can also separate the middleware into functions, to re-use it in many routes, for example:

```java
Handler jwtMiddleware = ctx -> {
    val authorizationHeader = ctx.getHeader().get("Authorization");
    if (!authorizationHeader.startsWith("Bearer")) {
        return HttpResponse.reject("Invalid token").status(401);
    }
    ctx.setDataParam("username", "tuhuynh");
    return HttpResponse.next();
};

// Or

class CommonMiddleware {
    static HttpResponse jwtMiddleware(Context ctx) {
        val authorizationHeader = ctx.getHeader().get("Authorization");
        if (!authorizationHeader.startsWith("Bearer")) {
            return HttpResponse.reject("Invalid token").status(401);
        }
        ctx.setDataParam("username", "tuhuynh");
        return HttpResponse.next();
    }
}
```

And re-use it like this:

```java
server.get("/protected", jwtMiddleware, 
    ctx -> HttpResponse.of("Login success, hello: "
                                            + ctx.getData("username")));

server.get("/submitTransaction", jwtMiddleware, 
    ctx -> HttpResponse.of("Submitted!"));
```

## Global

You can use the global middleware - the handler will be applied to all route path, in the below example, all request to the server will Print `Serving in: Current Thread`

```java
// Global middleware
server.use(ctx -> {
    val thread = Thread.currentThread().getName();
    System.out.println("Serving in " + thread);
    return HttpResponse.next();
});

server.use("/", ctx -> HttpResponse.of("Hello World"));
```
