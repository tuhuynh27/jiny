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

        server.use("/all", Cors.newHandler(), ctx -> HttpResponse.of("cors enabled for all"));

        server.use("/console",
                   Cors.newHandler(Config.builder()
                                         .allowAll(false)
                                         .allowOrigin(<your current domain in the browser where you open the console>)
                                         .build()),
                   ctx -> HttpResponse.of("google chrome console"));

        val origins = new ArrayList<String>();
        origins.add("http://localhost:8080");
        origins.add("http://example.com");
        server.use("/origins",
                   Cors.newHandler(Config.builder()
                                         .allowAll(false)
                                         .allowOrigins(origins)
                                         .build()),
                   ctx -> HttpResponse.of(origins.toString()));
        
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