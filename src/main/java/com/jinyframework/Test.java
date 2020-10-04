package com.jinyframework;

import com.jinyframework.core.RequestBinderBase.HttpResponse;
import lombok.val;

import java.io.IOException;

public final class Test {
    public static void main(String[] args) throws IOException {
        val server = NIOHttpServer
                .port(1234)
                .setNumOfEventLoopThread(9999)
                .setThreadDebugMode(true)
                .useTransformer(t -> t + "1");
        server.use("/", ctx -> HttpResponse.ofAsync("Hello World!"));
        server.start();
    }
}
