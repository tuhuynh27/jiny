package com.tuhuynh.jerrymouse;

import com.tuhuynh.jerrymouse.core.RequestBinder.HttpResponse;
import com.tuhuynh.jerrymouse.core.bio.HttpRouter;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("api.HttpServerTest")
public class HTTPServerTest {
    final int port = 1234;
    final String url = "http://localhost:" + port;

    @BeforeEach
    void each() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
    }

    @BeforeAll
    static void startServer() throws InterruptedException {
        new Thread(() -> {
            val server = HttpServer.port(1234);

            server.use(ctx -> {
                ctx.putHandlerData("global", "middleware");
                return HttpResponse.next();
            });

            server.get("/", ctx -> HttpResponse.of("Hello World"));
            server.get("/gm", ctx -> HttpResponse.of(ctx.getData("global")));
            server.get("/gm-sub", ctx -> HttpResponse.of(ctx.getData("att")));
            server.post("/echo", ctx -> HttpResponse.of(ctx.getBody()));
            server.get("/query", ctx -> {
                val world = ctx.getQuery().get("hello");
                return HttpResponse.of(world);
            });
            server.get("/path/:foo/:bar", ctx -> {
                val foo = ctx.getParam().get("foo");
                val bar = ctx.getParam().get("bar");
                return HttpResponse.of(foo + ":" + bar);
            });
            server.get("/all/**", ctx -> HttpResponse.of(ctx.getPath()));

            server.get("/protected",
                    ctx -> {
                        val authorizationHeader = ctx.getHeader().get("authorization");
                        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer")) {
                            return HttpResponse.reject("invalid_token").status(401);
                        }
                        ctx.putHandlerData("username", "tuhuynh");
                        return HttpResponse.next();
                    }, // Injected
                    ctx -> HttpResponse.of("success:" + ctx.getData("username")));

            server.get("/panic", ctx -> {
                throw new RuntimeException("Panicked!");
            });

            val catRouter = new HttpRouter();
            catRouter.use(ctx -> {
                ctx.putHandlerData("att", "cat");
                return HttpResponse.next();
            });
            catRouter.get("/", ctx -> HttpResponse.of("this is a cat"));
            catRouter.get("/gm", ctx -> HttpResponse.of(ctx.getData("att")));
            catRouter.post("/echo", ctx -> HttpResponse.of(ctx.getBody()));
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

    @Test
    @DisplayName("Hello World test")
    void helloWorld() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "Hello World");
    }

    @Test
    @DisplayName("Echo test")
    void echo() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/echo").method("POST").body("copycat")
                .build().perform();
        assertEquals(res.getBody(), "copycat");
    }

    @Test
    @DisplayName("Query Params test")
    void queryParams() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/query?hello=world").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "world");
    }

    @Test
    @DisplayName("Path Params test")
    void pathParams() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/path/hello/123").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "hello:123");
    }

    @Test
    @DisplayName("Catch All test")
    void catchAll() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/all/123/456").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "/all/123/456");
    }

    @Test
    @DisplayName("Middleware test failed")
    void middlewareFail() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/protected").method("GET")
                .build().perform();
        assertEquals(res.getStatus(), 401);
        assertEquals(res.getBody(), "invalid_token");
    }

    @Test
    @DisplayName("Middleware test success")
    void middlewareSuccess() throws IOException {
        val headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer test_token");
        val res = HttpClient.builder()
                .url(url + "/protected").method("GET").headers(headers)
                .build().perform();
        assertEquals(res.getStatus(), 200);
        assertEquals(res.getBody(), "success:tuhuynh");
    }

    @Test
    @DisplayName("Panic")
    void panic() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/panic").method("GET")
                .build().perform();
        assertEquals(res.getStatus(), 500);
        assertEquals(res.getBody(), "Panicked!");
    }

    @Test
    @DisplayName("Global Middleware")
    void globalMiddleware() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/gm").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "middleware");
    }

    @Test
    @DisplayName("Global Middleware not from subRouter")
    void globalMiddlewareNotFromSubRouter() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/gm").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "middleware");
    }

    @Test
    @DisplayName("SubRouter1")
    void subRouter1() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/cat").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "this is a cat");
    }

    @Test
    @DisplayName("SubRouter2")
    void subRouter2() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/cat/echo").method("POST").body("copycat")
                .build().perform();
        assertEquals(res.getBody(), "copycat");
    }

    @Test
    @DisplayName("SubRouter3")
    void subRouter3() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/cat/gm").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "cat");
    }
}
