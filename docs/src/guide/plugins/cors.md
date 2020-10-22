# CORS Middleware

CORS providing a Jiny middleware that can be used to enable CORS with various options.

Example:

```java
import com.jinyframework.middlewares.Cors;

public class CorsExample {
    public static void main(String[] args) throws IOException {
        val server = HttpServer.port(1234);

        server.use("/all", Cors.newHandler(), ctx -> HttpResponse.of("cors enabled for all"));

        server.use("/console",
                   Cors.newHandler(Config.builder()
                                         .allowAll(false)
                                         .allowOrigin("https://code.google.com")
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
fetch(url) -> cors error
fetch(url + "/all") -> success
fetch(url + "/console") -> success
fetch(url + "/origins") -> cors error
```