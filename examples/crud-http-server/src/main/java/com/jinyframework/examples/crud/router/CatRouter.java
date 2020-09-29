package com.jinyframework.examples.crud.router;

import com.jinyframework.examples.crud.storage.MongoDB;
import com.jinyframework.examples.crud.handlers.CatHandler;
import com.jinyframework.core.bio.HttpRouter;
import lombok.val;

public final class CatRouter {
    public static HttpRouter getRouter() {
        val mongoClient = MongoDB.init();
        val catHandler = new CatHandler(mongoClient);
        val router = new HttpRouter();
        router.get("/", catHandler::getCats);
        router.post("/", catHandler::addCat);
        router.get("/template", catHandler::template);
        return router;
    }
}
