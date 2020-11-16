package com.jinyframework;

import com.jinyframework.core.AbstractRequestBinder;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.core.bio.HttpRouter;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            server.get("/immediate", ctx -> HttpResponse.of("Foo"), ctx -> HttpResponse.of("Bar"));
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
            server.get("/data/param",ctx -> {
                ctx.setDataParam("string","data/param");
                val stream = Stream.of("a", "b");
                ctx.setDataParam("stream", stream);
                ctx.setDataParam("list",stream.collect(Collectors.toList()));
                ctx.setDataParam("map",new HashMap<>());
                ctx.setDataParam("context", AbstractRequestBinder.Context.builder().build());
                return HttpResponse.next();
            },ctx -> {
                if (!(ctx.dataParam("string") instanceof String)) {
                    return HttpResponse.reject("wrong class");
                }
                if (!(ctx.dataParam("stream") instanceof Stream)) {
                    return HttpResponse.reject("wrong class");
                }
                if (!(ctx.dataParam("list") instanceof List)) {
                    return HttpResponse.reject("wrong class");
                }
                if (!(ctx.dataParam("map") instanceof HashMap)) {
                    return HttpResponse.reject("wrong class");
                }
                if (!(ctx.dataParam("context") instanceof AbstractRequestBinder.Context)) {
                    return HttpResponse.reject("wrong class");
                }
                return HttpResponse.next();
            }, ctx -> HttpResponse.of("ok"));
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

            server.start();
        }).start();

        // Wait for server to start
        TimeUnit.SECONDS.sleep(3);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }
}
