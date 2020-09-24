package com.tuhuynh.niocrud;

import com.tuhuynh.jerrymouse.NIOHttpServer;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.HttpResponse;
import com.tuhuynh.niocrud.router.CatRouter;
import lombok.val;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public final class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        val server = NIOHttpServer.port(1234);
        server.get("/", ctx -> HttpResponse.ofAsync("CRUD"));
        val catRouter = CatRouter.getRouter();
        server.use("/cat", catRouter);
        server.start();
    }
}