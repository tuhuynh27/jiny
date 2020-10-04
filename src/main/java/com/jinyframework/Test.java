package com.jinyframework;

import lombok.val;

import java.io.IOException;

public final class Test {
    public static void main(String[] args) throws IOException {
        val server = HttpServer
                .port(1234)
                .useTransformer(t -> t + "1");
        server.start();
    }
}
