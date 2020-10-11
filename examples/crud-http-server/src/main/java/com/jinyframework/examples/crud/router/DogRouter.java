package com.jinyframework.examples.crud.router;

import com.jinyframework.core.bio.HttpRouter;
import com.jinyframework.examples.crud.handlers.DogHandler;
import lombok.val;

public class DogRouter {
    public static HttpRouter getRouter() {
        val dogHandler = new DogHandler();
        val router = new HttpRouter();
        router.get("/", dogHandler::getDogs);
        router.get("/:id", dogHandler::getDog);
        router.post("/", dogHandler::addDog);
        router.put("/:id", dogHandler::updateDog);
        router.delete("/:id", dogHandler::deleteDog);
        return router;
    }
}
