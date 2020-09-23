package com.tuhuynh.jerrymouse.tests;

import com.tuhuynh.jerrymouse.NIOHttpServer;
import com.tuhuynh.jerrymouse.core.RequestBinder.HttpResponse;
import com.tuhuynh.jerrymouse.core.nio.HttpRouter;
import lombok.val;

public class TestNIOServerRouter {
    public static void main(String[] args) throws Exception {
        val server = NIOHttpServer.port(1234);

        server.use("/", ctx -> HttpResponse.ofAsync("Hello World"));

        val testRouter = new HttpRouter();
        testRouter.get("/lol", ctx -> HttpResponse.ofAsync("LOL"));
        testRouter.get("/path", ctx -> HttpResponse.ofAsync(ctx.getPath()));

        server.use("/test", testRouter);

        server.start();
    }
}
