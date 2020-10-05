package com.jinyframework;

import com.jinyframework.core.RequestBinderBase.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public final class Test {
    public static void main(String[] args) throws IOException {
        val server = HttpServer
                .port(1234)
                .setThreadDebugMode(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }));

        server.get("/", ctx -> HttpResponse.of("Hello World!"));
        server.post("/echo", ctx -> HttpResponse.of(ctx.getBody()));
        server.get("/random", ctx -> {
            final int random = ThreadLocalRandom.current().nextInt(1, 100000000 + 1);
            return HttpResponse.of("Random number: " + random);
        });
        server.get("/fibo", ctx -> {
            final int fibo = fibonacci(35);
            return HttpResponse.of("Fibo number: " + fibo);
        });
        server.start();
    }

    public static int fibonacci(int n) {
        if (n < 0) {
            return -1;
        } else if (n == 0 || n == 1) {
            return n;
        } else {
            return fibonacci(n - 1) + fibonacci(n - 2);
        }
    }
}
