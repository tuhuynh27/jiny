package com.tuhuynh.httpserver.tests;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.tuhuynh.httpserver.HTTPServer;
import com.tuhuynh.httpserver.NIOHTTPServer;
import com.tuhuynh.httpserver.core.RequestBinder.HttpResponse;

import lombok.val;

public final class TestServers {
    public static void main(String[] args) {
        new Thread(() -> {
            val nioServer = NIOHTTPServer.port(1111);
            nioServer.use("/", ctx -> CompletableFuture.completedFuture(HttpResponse.of("Hello World")));
            nioServer.get("/thread", ctx -> CompletableFuture
                    .completedFuture(HttpResponse.of(Thread.currentThread().getName())));
            try {
                nioServer.start();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }).start();

        new Thread(() -> {
            val server = HTTPServer.port(2222);
            server.use("/", ctx -> HttpResponse.of("Hello World"));
            server.get("/thread", ctx -> HttpResponse.of(Thread.currentThread().getName()));
            try {
                server.start();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }).start();
    }
}
