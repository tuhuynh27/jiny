# Context

You can get request's attribute from Context, the below method is NPE handled (you will get an empty string if it is null)

## Query Params

You can use Context Object to extract query params

```java
// Get query params, ex: /query?hello=world
server.get("/query", ctx -> {
    val world = ctx.queryParam("hello");
    return HttpResponse.of("Hello: " + world);
});
```

## Path Params

You can use Context Object to extract path params

```java
// Get handler params, ex: /params/:categoryID/:itemID
server.get("/params/:categoryID/:itemID", ctx -> {
    val categoryID = ctx.pathParam("categoryID");
    val itemID = ctx.pathParam("itemID");
    return HttpResponse.of("Category ID is " + categoryID + ", Item ID is " + itemID);
});
```

## Header Params

You can use Context Object to extract header params (all attributed parsed with lowercase)

```java
// Get handler params, ex: /params/:categoryID/:itemID
server.get("/header", ctx -> {
    val authorization = ctx.headerParam("authorization");
    val userAgent = ctx.headerParam("user-agent");
    return HttpResponse.of("Authorization is " + authorization + ", UserAgent is " + userAgent);
});
```