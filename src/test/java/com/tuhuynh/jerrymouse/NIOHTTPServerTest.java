package com.tuhuynh.jerrymouse;

import com.tuhuynh.jerrymouse.core.RequestBinderBase.HttpResponse;
import com.tuhuynh.jerrymouse.core.nio.HttpRouter;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@DisplayName("api.NIOHttpServerTest")
public class NIOHTTPServerTest extends HTTPTest {
    public NIOHTTPServerTest() {
        this.url = "http://localhost:1235";
    }

    @BeforeAll
    static void startServer() throws InterruptedException {
        new Thread(() -> {
            val server = NIOHttpServer.port(1235);

            server.use(ctx -> {
                ctx.putHandlerData("global", "middleware");
                return HttpResponse.nextAsync();
            });

            server.get("/", ctx -> HttpResponse.ofAsync("Hello World"));
            server.get("/gm", ctx -> HttpResponse.ofAsync(ctx.getData("global")));
            server.get("/gm-sub", ctx -> HttpResponse.ofAsync(ctx.getData("att")));
            server.post("/echo", ctx -> HttpResponse.ofAsync(ctx.getBody()));
            server.get("/query", ctx -> {
                val world = ctx.getQuery().get("hello");
                return HttpResponse.ofAsync(world);
            });
            server.get("/path/:foo/:bar", ctx -> {
                val foo = ctx.getParam().get("foo");
                val bar = ctx.getParam().get("bar");
                return HttpResponse.ofAsync(foo + ":" + bar);
            });
            server.get("/all/**", ctx -> HttpResponse.ofAsync(ctx.getPath()));

            server.get("/protected",
                    ctx -> {
                        val authorizationHeader = ctx.getHeader().get("authorization");
                        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer")) {
                            return HttpResponse.rejectAsync("invalid_token", 401);
                        }
                        ctx.putHandlerData("username", "tuhuynh");
                        return HttpResponse.nextAsync();
                    }, // Injected
                    ctx -> HttpResponse.ofAsync("success:" + ctx.getData("username")));

            server.get("/panic", ctx -> {
                throw new RuntimeException("Panicked!");
            });

            val catRouter = new HttpRouter();
            catRouter.use(ctx -> {
                ctx.putHandlerData("att", "cat");
                return HttpResponse.nextAsync();
            });
            catRouter.get("/", ctx -> HttpResponse.ofAsync("this is a cat"));
            catRouter.get("/gm", ctx -> HttpResponse.ofAsync(ctx.getData("att")));
            catRouter.post("/echo", ctx -> HttpResponse.ofAsync(ctx.getBody()));
            catRouter.get("/:foo/:bar", ctx -> HttpResponse.ofAsync(ctx.getParam().get("foo") + ":" + ctx.getParam().get("bar")));
            server.use("/cat", catRouter);

            try {
                server.start();
            } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }).start();

        // Wait for server to start
        TimeUnit.SECONDS.sleep(3);
    }
}
