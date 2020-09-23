package com.tuhuynh.niocrud;

import com.tuhuynh.jerrymouse.NIOHttpServer;
import com.tuhuynh.niocrud.handlers.CatHandler;
import com.tuhuynh.niocrud.storage.MongoDB;

import lombok.val;

public final class Router {
    public static void initRouter(NIOHttpServer server) {
        val mongoClient = MongoDB.init();
        val catHandler = new CatHandler(mongoClient);
        server.get("/cats", catHandler::getCats);
        server.post("/cats", catHandler::addCat);
    }
}
