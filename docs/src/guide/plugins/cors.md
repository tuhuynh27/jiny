# CORS Middleware

Cross-origin resource sharing (CORS) is a mechanism that allows restricted resources on a web page to be requested
 from another domain outside the domain from which the first resource was served. REST APIs built in Jiny will require
  a CORS policy in order to safely return requests to modern web browsers.
  
## Install

`build.gradle`

```groovy
dependencies {
    compile group: 'com.jinyframework', name: 'middlewares', version: 'x.x.x'
}
```

## Configuration
  
An example configuration could look something like this:

```java
import com.jinyframework.middlewares.Cors;

public class CorsExample {
    public static void main(String[] args) throws IOException {
        val server = HttpServer.port(1234);
  
        server.use("/default", Cors.newHandler(Cors.allowDefault()),
                   ctx -> HttpResponse.of("cors default settings"));
        server.use("/all", Cors.newHandler(Cors.allowAll()),
                   ctx -> HttpResponse.of("cors enabled for all"));

        val defaultBased = Cors.Config.defaultBuilder()
                                          .allowOrigin("*")
                                          .exposeHeader("Bar")
                                          .build();

        server.use("/extend-default", Cors.newHandler(defaultBased),
                   ctx -> HttpResponse.of("extend from default settings"));

        val allowMethods = Stream.of("GET", "POST", "HEAD", "PUT").collect(Collectors.toList());
        val allowHeaders = Stream.of("Origin", "Accept", "Content-Type", "X-Requested-With",
                                                    "Bar").collect(Collectors.toList());
        val allowConsole = Cors.Config.builder()
                                          .allowAllOrigins(false)
                                          .allowCredentials(true)
                                          .allowOrigin("http://localhost:8080")
                                          .exposeHeader("Foo")
                                          .allowMethods(allowMethods)
                                          .allowHeaders(allowHeaders)
                                          .build();
        server.use("/console",
                   Cors.newHandler(allowConsole),
                   ctx -> HttpResponse.of("google chrome console"));
        
        server.start();
    }
}
```

Test with fetch in google chrome console

```javascript
var url = "http://localhost:1234"
fetch(url) // Result: cors error
fetch(url + "/all") // Result: success
fetch(url + "/console") // Result: success
fetch(url + "/origins") // Result: cors error
```

::: warning
Given that thrown errors are immediately returned to the client, the CORSMiddleware must be listed before the ErrorMiddleware. Otherwise, the HTTP error response will be returned without CORS headers, and cannot be read by the browser.
:::