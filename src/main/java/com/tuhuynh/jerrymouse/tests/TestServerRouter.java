package com.tuhuynh.jerrymouse.tests;

import com.tuhuynh.jerrymouse.HttpServer;
import com.tuhuynh.jerrymouse.core.RequestBinder.HttpResponse;
import com.tuhuynh.jerrymouse.core.bio.HttpRouter;
import lombok.val;

import java.io.IOException;

public class TestServerRouter {
    public static void main(String[] args) throws IOException {
        val server = HttpServer.port(1234);

        server.use("/", ctx -> HttpResponse.of("Hello World"));
        server.post("/echo", ctx -> HttpResponse.of(ctx.getBody()));

        val testRouter = new HttpRouter();
        testRouter.use(ctx -> {
            System.out.println("Applied");
            return HttpResponse.next();
        });

        testRouter.get("/lol", ctx -> HttpResponse.of(ctx.getPath()));
        testRouter.get("/heh", ctx -> HttpResponse.of("Heh"));

        server.use("/test", testRouter);

        server.start();
    }
}
