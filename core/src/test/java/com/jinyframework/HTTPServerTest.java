package com.jinyframework;

import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.core.bio.HttpRouter;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@DisplayName("api.HttpServerTest")
public class HTTPServerTest extends HTTPTest {
    private static final HttpServer server = HttpServer.port(1234);

    public HTTPServerTest() {
        this.url = "http://localhost:1234";
    }

    @BeforeAll
    static void startServer() throws InterruptedException {
        new Thread(() -> {
            server.use(ctx -> {
                ctx.setDataParam("global", "middleware");
                return HttpResponse.next();
            });

            val defaultResponseHeaders = new HashMap<String, String>();
            defaultResponseHeaders.put("Content-Type", "application/json");
            server.useResponseHeaders(defaultResponseHeaders);

            server.get("/", ctx -> HttpResponse.of("Hello World"));
            server.post("/transform", ctx -> HttpResponse.of(ctx.getBody()).transform(s -> s + "ed"));
            server.get("/gm", ctx -> HttpResponse.of(ctx.dataParam("global")));
            server.get("/gm-sub", ctx -> HttpResponse.of(ctx.dataParam("att")));
            server.post("/echo", ctx -> HttpResponse.of(ctx.getBody()));
            server.get("/req-header", ctx -> HttpResponse.of(ctx.headerParam("foo")
                    + ctx.headerParam("Bar")));
            server.get("/header", ctx -> {
                ctx.putHeader("foo", "bar");
                return HttpResponse.of("Done!");
            });
            server.get("/query", ctx -> {
                val world = ctx.queryParam("hello");
                return HttpResponse.of(world);
            });
            server.get("/path/:foo/:bar", ctx -> {
                val foo = ctx.pathParam("foo");
                val bar = ctx.pathParam("bar");
                return HttpResponse.of(foo + ":" + bar);
            });
            server.get("/all/**", ctx -> HttpResponse.of(ctx.getPath()));

            server.get("/protected",
                    ctx -> {
                        val authorizationHeader = ctx.getHeader().get("authorization");
                        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer")) {
                            return HttpResponse.reject("invalid_token").status(401);
                        }
                        ctx.setDataParam("username", "tuhuynh");
                        return HttpResponse.next();
                    }, // Injected
                    ctx -> HttpResponse.of("success:" + ctx.getData().get("username")));

            server.get("/panic", ctx -> {
                throw new RuntimeException("Panicked!");
            });

            val catRouter = new HttpRouter();
            catRouter.use(ctx -> {
                ctx.getData().put("att", "cat");
                return HttpResponse.next();
            });
            catRouter.get("/", ctx -> HttpResponse.of("this is a cat"));
            catRouter.get("/gm", ctx -> HttpResponse.of(ctx.dataParam("att")));
            catRouter.post("/test", ctx -> HttpResponse.of(ctx.getBody()));
            catRouter.get("/:foo/:bar", ctx -> HttpResponse.of(ctx.pathParam("foo") + ":" + ctx.pathParam("bar")));
            server.use("/cat", catRouter);

            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Wait for server to start
        TimeUnit.SECONDS.sleep(3);
    }

    @AfterAll
    static void stopServer() throws IOException {
        server.stop();
    }
}
