package com.jinyframework;

import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.core.nio.HttpRouterNIO;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@DisplayName("api.NIOHttpServerTest")
public class NIOHTTPServerTest extends HTTPTest {
    private static final NIOHttpServer server = NIOHttpServer.port(1235);

    public NIOHTTPServerTest() {
        this.url = "http://localhost:1235";
    }

    @BeforeAll
    static void startServer() throws InterruptedException {
        new Thread(() -> {
            server.use(ctx -> {
                ctx.setDataParam("global", "middleware");
                return HttpResponse.nextAsync();
            });

            server.get("/", ctx -> HttpResponse.ofAsync("Hello World"));
            server.post("/transform", ctx -> HttpResponse.ofAsync(ctx.getBody(), s -> s + "ed"));
            server.get("/gm", ctx -> HttpResponse.ofAsync(ctx.dataParam("global")));
            server.get("/gm-sub", ctx -> HttpResponse.ofAsync(ctx.dataParam("att")));
            server.post("/echo", ctx -> HttpResponse.ofAsync(ctx.getBody()));
            server.get("/req-header", ctx -> HttpResponse.ofAsync(ctx.headerParam("foo")
                    + ctx.headerParam("Bar")));
            server.get("/header", ctx -> {
                ctx.putHeader("foo", "bar");
                return HttpResponse.ofAsync("Done!");
            });
            server.get("/query", ctx -> {
                val world = ctx.queryParam("hello");
                return HttpResponse.ofAsync(world);
            });
            server.get("/path/:foo/:bar", ctx -> {
                val foo = ctx.pathParam("foo");
                val bar = ctx.pathParam("bar");
                return HttpResponse.ofAsync(foo + ":" + bar);
            });
            server.get("/all/**", ctx -> HttpResponse.ofAsync(ctx.getPath()));

            server.get("/protected",
                    ctx -> {
                        val authorizationHeader = ctx.getHeader().get("authorization");
                        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer")) {
                            return HttpResponse.rejectAsync("invalid_token", 401);
                        }
                        ctx.getData().put("username", "tuhuynh");
                        return HttpResponse.nextAsync();
                    }, // Injected
                    ctx -> HttpResponse.ofAsync("success:" + ctx.getData().get("username")));

            server.get("/panic", ctx -> {
                throw new RuntimeException("Panicked!");
            });

            val catRouter = new HttpRouterNIO();
            catRouter.use(ctx -> {
                ctx.setDataParam("att", "cat");
                return HttpResponse.nextAsync();
            });
            catRouter.get("/", ctx -> HttpResponse.ofAsync("this is a cat"));
            catRouter.get("/gm", ctx -> HttpResponse.ofAsync(ctx.dataParam("att")));
            catRouter.post("/test", ctx -> HttpResponse.ofAsync(ctx.getBody()));
            catRouter.get("/:foo/:bar", ctx -> HttpResponse.ofAsync(ctx.pathParam("foo") + ":" + ctx.pathParam("bar")));
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
