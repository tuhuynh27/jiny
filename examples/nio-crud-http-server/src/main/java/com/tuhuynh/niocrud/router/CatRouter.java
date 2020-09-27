package com.tuhuynh.niocrud.router;

import com.tuhuynh.jerrymouse.core.nio.HttpRouterNIO;
import com.tuhuynh.niocrud.handlers.CatHandler;
import com.tuhuynh.niocrud.storage.MongoDB;
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
