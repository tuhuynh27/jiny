package com.jinyframework;

import com.jinyframework.core.RequestBinderBase.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;

@Slf4j
public final class Test {
    public static void main(String[] args) throws IOException {
        val server = HttpServer
                .port(1234)
                .setThreadDebugMode(true)
                .useTransformer(t -> t + "1");
        server.get("/", ctx -> {
            log.info("Current threads number: " + Thread.getAllStackTraces().keySet().size());
            return HttpResponse.of("Hello World!");
        });
        server.start();
    }
}
