package com.tuhuynh.httpserver.tests;

import java.util.concurrent.Executors;

import com.tuhuynh.httpserver.NIOHTTPServer;
import com.tuhuynh.httpserver.core.RequestBinderBase.HttpResponse;
import com.tuhuynh.httpserver.core.nio.AsyncHelper;

import lombok.val;

public final class TestNIOServer {
    public static void main(String[] args) throws Exception {
        val workerPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        val server = NIOHTTPServer.port(1234);

        server.use("/", ctx -> HttpResponse.ofAsync("Hello World"));

        server.get("/thread", ctx -> HttpResponse.ofAsync(Thread.currentThread().getName()));

        // This request will not block the main thread (event loop)
        server.get("/sleep", ctx -> {
            val async = AsyncHelper.make();

            workerPool.submit(() -> {
                try {
                    // Some expensive / blocking task
                    val thread = Thread.currentThread().getName();
                    System.out.println("Executing an expensive task on " + thread);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }

                val thread = Thread.currentThread().getName();
                async.resolve("Work has done, current thread is: " + thread);
            });

            return async.submit();
        });

        // This request will block one of the event loop thread
        // By default you have cpu.length * 2 event loop thread
        server.use("/block", ctx -> {
            System.out.println(Thread.currentThread().getName() + " is gonna be blocked now!");
            Thread.sleep(60 * 1000); // Block for 60s
            return HttpResponse.ofAsync("Block one event loop thread!");
        });

        // Middleware
        server.get("/protected", ctx -> {
            val authorizationHeader = ctx.getHeader().get("Authorization");
            if (!authorizationHeader.startsWith("Bearer ")) {
                return HttpResponse.rejectAsync("InvalidToken", 401);
            }
            ctx.putHandlerData("username", "tuhuynh");
            return HttpResponse.nextAsync();
        }, ctx -> HttpResponse.ofAsync("Login success, hello: " + ctx.getData("username")));

        server.start();
    }
}
