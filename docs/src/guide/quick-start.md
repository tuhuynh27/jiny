# Quick Start

You have your REST API ready to serve JSON in less than ten lines of code

:::: tabs

::: tab Java
 ```java
import com.jinyframework.*;
import com.google.gson.Gson;

public class Main {
    public static void main(String args[]) {
        val server = HTTPServer.port(1234);
        val gson = new Gson();
        server.useTransformer(gson::toJson);
        server.get("/ping", ctx -> HttpResponse.of("Pong"));
        server.start();
    }
} 
```
:::


::: tab Scala
```scala
import com.jinyframework.*
import your.server.factories.app.AppFactory

object Main extends App {
  val server = HttpServer.port(1234)
  server.useTransformer(s => AppFactory.getGson.toJson(s))
  server.get("/ping", ctx => HttpResponse.of("Pong))
  server.start()
}
```
:::

::: tab Kotlin
```kotlin
import com.jinyframework.*
import com.google.gson.Gson;

fun main(args: Array<String>) {
    val server: HttpServer = HTTPServer.port(1234)
    val gson = new Gson()
    server.useTransformer(gson::toJson);
    server.get("/ping", ctx -> HttpResponse.of("Pong"))
    server.start()
}
```
:::

::::

::: details What is ctx -> HttpResponse.of("Pong") ?

This is called [Functional Interface](https://www.geeksforgeeks.org/functional-interfaces-java/) - a shorthand syntax of Java 8, if you use Java 7 and before, you can write like this:

```java
server.use("/", new Handler() {
    @Override
    public HttpResponse handleFunc(Context context) throws Exception {
        return HttpResponse.of("Hello World!");
    }
});
```
:::

::: details Okay, then what is gson::toJson ?
It's another sugar syntax called [Double Colon Operator](https://www.baeldung.com/java-8-double-colon-operator)

The code above can be written without Double Colon Operator like this:

```java
server.useTransformer(response -> gson.toJson(response));
```

or without Double Colon Operator and Functional Interface should be as follows:

```java
server.useTransformer(new RequestTransformer() {
    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }
});
```
:::
