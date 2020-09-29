# Context

You can get request's attribute from Context

## Query Params

You can use Context Object to extract query params

```java
// Get query params, ex: /query?hello=world
server.get("/query", ctx -> {
    val world = ctx.getQuery().get("hello");
    return HttpResponse.of("Hello: " + world);
});
```

## Path Params

You can use Context Object to extract path params

```java
// Get handler params, ex: /params/:categoryID/:itemID
server.get("/params/:categoryID/:itemID", ctx -> {
    val categoryID = ctx.getParam().get("categoryID");
    val itemID = ctx.getParam().get("itemID");
    return HttpResponse.of("Category ID is " + categoryID + ", Item ID is " + itemID);
});
```
