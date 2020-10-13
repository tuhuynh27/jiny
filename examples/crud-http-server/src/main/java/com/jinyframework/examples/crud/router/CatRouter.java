package com.jinyframework.examples.crud.router;

import com.jinyframework.core.bio.HttpRouter;
import com.jinyframework.examples.crud.handlers.CatHandler;
import lombok.val;

public class CatRouter {
    public static HttpRouter getRouter() {
        val catHandler = new CatHandler();
        val router = new HttpRouter();
        router.get("/", catHandler::getCats);
        router.post("/", catHandler::addCat);
        router.get("/template", catHandler::template);
        return router;
    }
}
