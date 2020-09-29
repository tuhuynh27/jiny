package com.jinyframework.examples.niocrud.router;

import com.jinyframework.core.nio.HttpRouterNIO;
import com.jinyframework.examples.niocrud.handlers.CatHandler;
import com.jinyframework.examples.niocrud.storage.MongoDB;
import lombok.val;

public final class CatRouter {
    public static HttpRouterNIO getRouter() {
        val mongoClient = MongoDB.init();
        val catHandler = new CatHandler(mongoClient);
        val router = new HttpRouterNIO();
        router.get("/", catHandler::getCats);
        router.post("/", catHandler::addCat);
        return router;
    }
}
