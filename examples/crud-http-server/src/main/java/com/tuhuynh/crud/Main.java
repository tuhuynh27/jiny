package com.tuhuynh.crud;

import java.io.IOException;

import com.tuhuynh.jerrymouse.HttpServer;
import com.tuhuynh.jerrymouse.core.RequestBinder.HttpResponse;

import lombok.val;

public final class Main {
    public static void main(String[] args) throws IOException {
        val server = HttpServer.port(1234);
        server.get("/", ctx -> HttpResponse.of("CRUD"));
        Router.initRouter(server);
        server.start();
    }
}