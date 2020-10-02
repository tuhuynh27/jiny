# Project Structure

Suggested project structure for project using Jiny framework

## Handler class

```java
class WebhookHandler {
    private Map<String, String> teachDict;
    
    public WebhookHandler() throws IOException {
        val data = read(); // Read data from persistence disk
        teachDict = data.isEmpty() ? new HashMap<>() : data
    }
    
    public HttpResponse showDict(final RequestContext context) {
        return HttpResponse.of(teachDict);
    }

    public HttpResponse setDict(final RequestContext context) {
        val body = context.getBody();
        val type = new TypeToken<HashMap<String, String>>() {}.getType();
        teachDict = gson.fromJson(body, type);
        sync(); // Sync data to persistence disk
        return HttpResponse.of("Done");
    }
}
```

## Router class

```java
import com.your.app.handlers.WebhookHandler;

class WebhookRouter {
    public static HttpRouter getRouter() {
        val router = new HttpRouter();
        server.get("/dict", webhookHandler::showDict);
        server.post("/dict", commonHandler::jwtMiddleware,
                             webhookHandler::setDict);
        return router;
    }
}
```

## Main class

```java
class Main {
    static void main(String[] args) throws IOException {
        val server = HttpServer.port(1234);
        val webhookHandler = new WebhookHandler();
        val gson = new Gson();
        server.setResponseTransformer(gson::toJson);
        server.use(commonHandler::loggingMiddleware);
        server.use("/webhook", WebHookRouter.getRouter());
    }
}
```
