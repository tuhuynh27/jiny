# Renderer

## Transform

You can use a JSON adapter to custom the response, recommended to use [Google's GSON](https://github.com/google/gson)

```java
import com.google.gson.Gson;

server.get("/json", ctx -> {
    val customer = CustomObject.builder()
                               .email("abc@gmail.com")
                               .name("Tu Huynh").build();
    val gson = new Gson();
    return HttpResponse.of(customer)
                       .transform(gson::toJson);
    // You will get {"email": "abc@gmail.com", "name": "Tu Huynh"}
});
```

::: details JSON/XML?
You can also use a [XML transformer](https://docs.oracle.com/javase/7/docs/api/javax/xml/transform/Transformer.html), to make your Jiny server speak `XML` or whatever format you want)
:::

## Global Transform

You can also set a global custom response, which will be applied for all handlers in the server

```java
import com.google.gson.Gson;

val server = HttpServer.port(1234)
            .useTransformer(gson::toJson);
```

## Template Engine

You can use a template engine as a custom render:

```java
import com.github.jknack.handlebars.Handlebars;

public HttpResponse index(RequestContext ctx) throws IOException {
    val hb = new Handlebars();
    val template = hb.compileInline("<b>Hello {{this}}</b>");
    return HttpResponse.of(template.apply(ctx.getQuery().get("name")));
}
```
