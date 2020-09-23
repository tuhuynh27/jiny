package com.tuhuynh.niocrud;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.tuhuynh.jerrymouse.NIOHttpServer;
import com.tuhuynh.jerrymouse.core.RequestBinder.HttpResponse;

import lombok.val;

public final class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        val server = NIOHttpServer.port(1234);
        server.get("/", ctx -> HttpResponse.ofAsync("CRUD"));
        Router.initRouter(server);
        server.start();
    }
}