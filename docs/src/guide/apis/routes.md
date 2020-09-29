# Routes

## Define

The main building block of a Jiny HTTP Server is a set of routes. A route is made up of three simple pieces:

- A verb (`get`, `post`, `put`, `delete`)
- A path (`/hello`, `/users/:name`)
- A handler `(ctx) -> { doSomething() }`

Routes are matched in the order they are defined. The first route that matches the request is invoked.

```java
server.get("/get", ctx -> {
    return HttpResponse.of("Get");
});

server.post("/post", ctx -> {
    return HttpResponse.of("Post");
});

server.put("/get", ctx -> {
    return HttpResponse.of("Put");
});

server.delete("/delete", ctx -> {
    return HttpResponse.of("Delete");
});

server.addHandler(RequestMethod.PATCH, "/custom", ctx -> {
    return HttpResponse.of("Custom");
});
```

## Catch All

This route config will match all request path start with `/all` such as `/all/123` or `/all/foo/bar`

```java
// Catch all
server.get("/all/**", ctx -> HttpResponse.of(ctx.getPath()));
```

## Router

Use the `HttpRouter` class to create modular, mountable route handlers. An `HttpRouter` instance is a complete middleware and routing system; for this reason, it is often referred to as a “mini-server”.

```
val catRouter = new HttpRouter();

catRouter.use(ctx -> {
    System.out.println("This is a middleware of Cat router");
    return HttpResponse.next();
});

catRouter.get("/", ctx -> HttpResponse.of("Cat"));
catRouter.get("/lol", ctx -> HttpResponse.of(ctx.getPath()));
catRouter.get("/heh", ctx -> HttpResponse.of("Heh"));
```

Then mount this `catRouter` to `server`

```
val server = HttpServer.port(1234);
server.use("/test", catRouter);
```
