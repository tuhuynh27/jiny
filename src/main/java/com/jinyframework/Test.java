package com.jinyframework;

import com.jinyframework.core.RequestBinderBase.HttpResponse;
import lombok.val;

import java.io.IOException;

public final class Test {
    public static void main(String[] args) throws IOException {
        val server = NIOHttpServer.port(1234);

        // CORS middleware
        server.use(ctx -> {
            ctx.setResponseHeader("Access-Control-Allow-Origin", "*");
            return HttpResponse.nextAsync();
        });

        server.get("/", ctx -> HttpResponse.ofAsync("Hello World!"));

        server.start();
    }
}
