package com.tuhuynh.httpserver.tests;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import com.tuhuynh.httpserver.core.RequestBinder.HttpResponse;
import com.tuhuynh.httpserver.NIOHTTPServer;

import lombok.val;

public final class TestNIOServer {
    public static void main(String[] args) throws Exception {
        val workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        val server = NIOHTTPServer.port(1234);

        server.use("/", ctx -> CompletableFuture.supplyAsync(() -> HttpResponse.of("Hello World")));

        server.get("/thread", ctx -> CompletableFuture.supplyAsync(() -> HttpResponse.of(Thread.currentThread().getName())));

        server.get("/sleep", ctx -> {
            CompletableFuture<HttpResponse> completableFuture = new CompletableFuture<>();

            workerPool.submit(() -> {
                try {
                    // Some expensive task
                    val thread = Thread.currentThread().getName();
                    System.out.println("Executing an enpensive task on " + thread);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }

                val thread = Thread.currentThread().getName();
                completableFuture.complete(HttpResponse.of("Work has done, current thread is: " + thread));
            });

            return HttpResponse.promise(completableFuture);
        });

        server.start();
    }
}
