package com.tuhuynh.jerrymouse.tests;

import com.tuhuynh.jerrymouse.HttpClient;
import com.tuhuynh.jerrymouse.HttpServer;
import com.tuhuynh.jerrymouse.core.RequestBinder.HttpResponse;
import lombok.Builder;
import lombok.val;

import java.io.IOException;
import java.util.Random;

public final class TestServer {
    public static void main(String[] args) throws IOException {
        val server = HttpServer.port(1234);

        server.use("/", ctx -> HttpResponse.of("Hello World"));
        server.post("/echo", ctx -> HttpResponse.of(ctx.getBody()));

        // Free to execute blocking tasks with a Cached ThreadPool
        server.get("/sleep", ctx -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            return HttpResponse.of("Sleep done!");
        });

        server.get("/thread",
                   ctx -> HttpResponse.of(Thread.currentThread().getName()));

        server.get("/random", ctx -> {
            val rand = new Random();
            return HttpResponse.of(String.valueOf(rand.nextInt(100 + 1)));
        });

        // Get query params, ex: /query?hello=world
        server.get("/query", ctx -> {
            val world = ctx.getQuery().get("hello");
            return HttpResponse.of("Hello: " + world);
        });

        // Get handler params, ex: /params/:categoryID/:itemID
        server.get("/params/:categoryID/:itemID", ctx -> {
            val categoryID = ctx.getParam().get("categoryID");
            val itemID = ctx.getParam().get("itemID");
            return HttpResponse.of("Category ID is " + categoryID + ", Item ID is " + itemID);
        });

        // Catch all
        server.get("/all/**", ctx -> HttpResponse.of(ctx.getPath()));

        // Middleware support
        server.get("/protected", // You wanna provide a jwt validator on this endpoint
                   ctx -> {
                       val authorizationHeader = ctx.getHeader().get("authorization");
                       // Check JWT is valid, below is just a sample check
                       if (!authorizationHeader.startsWith("Bearer")) {
                           return HttpResponse.reject("Invalid token").status(401);
                       }
                       ctx.putHandlerData("username", "tuhuynh");
                       return HttpResponse.next();
                   }, // Injected
                   ctx -> HttpResponse.of("Login success, hello: " + ctx.getData("username")));

        // Global middleware
        server.use(ctx -> {
            val thread = Thread.currentThread().getName();
            System.out.println("Serving in " + thread);
            return HttpResponse.next();
        });

        // Perform as a proxy server
        server.get("/meme", ctx -> {
            // Built-in HTTP Client
            val meme = HttpClient.builder()
                                 .url("https://meme-api.herokuapp.com/gimme")
                                 .method("GET")
                                 .build().perform();
            return HttpResponse.of(meme.getBody())
                               .status(meme.getStatus());
        });

        // Handle error
        server.get("/panic", ctx -> {
            throw new RuntimeException("Panicked!");
        });

        server.start();
    }

    @Builder
    static class CustomObject {
        String email;
        String name;
    }
}
