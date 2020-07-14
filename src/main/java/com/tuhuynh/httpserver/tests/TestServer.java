package com.tuhuynh.httpserver.tests;

import java.io.IOException;
import java.util.Random;

import com.tuhuynh.httpserver.HTTPClient;
import com.tuhuynh.httpserver.HTTPServer;
import com.tuhuynh.httpserver.core.RequestBinderBase.HttpResponse;
import com.tuhuynh.httpserver.core.RequestBinderBase.RequestHandlerBIO;

import lombok.val;

public final class TestServer {
    public static void main(String[] args) throws IOException {
        val server = HTTPServer.port(1234);

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

        // Middleware support: Sample JWT Verify Middleware
        RequestHandlerBIO jwtValidator = ctx -> {
            val authorizationHeader = ctx.getHeader().get("Authorization");
            // Check JWT is valid, below is just a sample check
            if (!authorizationHeader.startsWith("Bearer")) {
                return HttpResponse.reject("Invalid token").status(401);
            }
            ctx.putHandlerData("username", "tuhuynh");
            return HttpResponse.next();
        };
        // Then, inject middleware to the request function chain like this
        server.get("/protected",
                   jwtValidator, // jwtMiddleware
                   ctx -> HttpResponse.of("Login success, hello: " + ctx.getData("username")));

        // Global middleware
        server.use(ctx -> {
            if (!"application/json".equals(ctx.getHeader().get("content-type").toLowerCase())) {
                return HttpResponse.reject("Only support RESTful API").status(403);
            }

            return HttpResponse.next();
        });

        // Perform as a proxy server
        server.get("/meme", ctx -> {
            // Built-in HTTP Client
            val meme = HTTPClient.builder()
                                 .url("https://meme-api.herokuapp.com/gimme")
                                 .method("GET")
                                 .build().perform();
            return HttpResponse.of(meme.getBody())
                               .status(meme.getStatus());
        });

        // Handle error
        server.get("/panic", ctx -> {
            throw new Exception("Panicked!");
        });

        server.start();
    }
}
