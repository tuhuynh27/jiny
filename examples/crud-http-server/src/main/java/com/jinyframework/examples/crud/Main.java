package com.jinyframework.examples.crud;

import com.jinyframework.HttpServer;
import com.jinyframework.core.RequestBinderBase.HttpResponse;
import com.jinyframework.examples.crud.factories.AppFactory;
import com.jinyframework.examples.crud.router.CatRouter;
import lombok.val;

import java.io.IOException;

public final class Main {
    public static void main(String[] args) throws IOException {
        val server = HttpServer.port(1234);
        server.useTransformer(res -> AppFactory.getGsonInstance().toJson(res));

        server.get("/", ctx -> HttpResponse.of("CRUD"));
        val catRouter = CatRouter.getRouter();
        server.use("/cat", catRouter);

        server.start();
    }
}