package com.jinyframework.examples.crud.router;

import com.jinyframework.core.bio.HttpRouter;
import com.jinyframework.examples.crud.handlers.TigerHandler;
import lombok.val;

public class TigerRouter {
    public static HttpRouter getRouter() {
        val tigerHandler = new TigerHandler();
        val router = new HttpRouter();
        router.get("/", tigerHandler::getTigers);
        router.get("/:id", tigerHandler::getTiger);
        router.post("/", tigerHandler::addTiger);
        return router;
    }
}
