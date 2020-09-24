package com.tuhuynh.crud;

import com.tuhuynh.crud.router.CatRouter;
import com.tuhuynh.jerrymouse.HttpServer;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.HttpResponse;
import lombok.val;

import java.io.IOException;

public final class Main {
    public static void main(String[] args) throws IOException {
        val server = HttpServer.port(1234);
        server.get("/", ctx -> HttpResponse.of("CRUD"));
        val catRouter = CatRouter.getRouter();
        server.use("/cat", catRouter);
        server.start();
    }
}