package com.tuhuynh.crud.router;

import com.tuhuynh.crud.handlers.CatHandler;
import com.tuhuynh.crud.storage.MongoDB;
import com.tuhuynh.jerrymouse.core.bio.HttpRouter;
import lombok.val;

public final class CatRouter {
    public static HttpRouter getRouter() {
        val mongoClient = MongoDB.init();
        val catHandler = new CatHandler(mongoClient);
        val router = new HttpRouter();
        router.get("/", catHandler::getCats);
        router.post("/", catHandler::addCat);
        return router;
    }
}
