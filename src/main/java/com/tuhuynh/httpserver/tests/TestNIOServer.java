package com.tuhuynh.httpserver.tests;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import com.tuhuynh.httpserver.core.RequestBinder.HttpResponse;
import com.tuhuynh.httpserver.experiments.NIOHTTPServer;

import lombok.val;

public final class TestNIOServer {
    public static void main(String[] args) throws Exception {
        val server = NIOHTTPServer.port(1234);

        server.get("/thread", ctx -> CompletableFuture.supplyAsync(() -> HttpResponse.of(Thread.currentThread().getName())));

        server.get("/sleep", ctx -> {
            CompletableFuture<HttpResponse> completableFuture = new CompletableFuture<>();

            Executors.newCachedThreadPool().submit(() -> {
                try {
                    Thread.sleep(2000);
                    val thread = Thread.currentThread().getName();
                    System.out.println("Worker: " + thread);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
                ctx.putHandlerData("fuck", "you");
                completableFuture.complete(HttpResponse.next());
            });

            return HttpResponse.promise(completableFuture);
        }, ctx -> {
            CompletableFuture<HttpResponse> completableFuture = new CompletableFuture<>();

            Executors.newCachedThreadPool().submit(() -> {
                try {
                    Thread.sleep(5000);
                    val thread = Thread.currentThread().getName();
                    System.out.println("Worker: " + thread);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
                val fuck = ctx.getData().get("fuck");
                completableFuture.complete(HttpResponse.of(fuck));
            });

            return HttpResponse.promise(completableFuture);
        });

        server.start();
    }
}
