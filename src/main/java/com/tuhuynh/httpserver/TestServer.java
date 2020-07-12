package com.tuhuynh.httpserver;

import java.io.IOException;
import java.util.Random;

import com.tuhuynh.httpserver.handlers.HandlerBinder.HttpResponse;
import com.tuhuynh.httpserver.utils.HandlerUtils.RequestMethod;

public final class TestServer {
    public static void main(String[] args) throws IOException {
        final HTTPServer server = new HTTPServer(8080);

        server.addHandler(RequestMethod.GET, "/", ctx -> HttpResponse.of("Hello World"));
        server.addHandler(RequestMethod.POST, "/echo", ctx -> HttpResponse.of(ctx.getPayload()));

        server.addHandler(RequestMethod.GET, "/sleep", ctx -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            return HttpResponse.of("Sleep done!");
        });
        server.addHandler(RequestMethod.GET, "/thread",
                          ctx -> HttpResponse.of(Thread.currentThread().getName()));

        server.addHandler(RequestMethod.GET, "/random", ctx -> {
            final Random rand = new Random();
            return HttpResponse.of(String.valueOf(rand.nextInt(100 + 1)));
        });

        server.addHandler(RequestMethod.GET, "/panic", ctx -> HttpResponse.of("Panic").status(500));

        server.start();
    }
}
