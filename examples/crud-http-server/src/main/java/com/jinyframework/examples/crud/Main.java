package com.jinyframework.examples.crud;

import com.jinyframework.HttpServer;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.examples.crud.factories.AppFactory;
import com.jinyframework.examples.crud.router.CatRouter;
import com.jinyframework.examples.crud.router.DogRouter;
import com.jinyframework.examples.crud.router.TigerRouter;
import com.jinyframework.middlewares.cors.Cors;
import lombok.val;

public class Main {
    public static void main(String[] args) {
        val server = HttpServer.port(1234);
        server.useTransformer(res -> AppFactory.getGson().toJson(res));
        server.use(Cors.newHandler(Cors.allowAll())); // CORS Config

        server.get("/", ctx -> HttpResponse.of("CRUD"));
        val catRouter = CatRouter.getRouter();
        val dogRouter = DogRouter.getRouter();
        val tigerRouter = TigerRouter.getRouter();
        server.use("/cats", catRouter);
        server.use("/dogs", dogRouter);
        server.use("/tigers", tigerRouter);

        server.start();
    }
}