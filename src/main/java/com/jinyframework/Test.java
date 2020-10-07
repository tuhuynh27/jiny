package com.jinyframework;

import com.jinyframework.core.RequestBinderBase.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;

@Slf4j
public final class Test {
    public static void main(String[] args) throws IOException {
        new Thread(() -> {
            val proxy = HttpProxy.port(1234);
            proxy.use("/server1", "localhost:1111");
            proxy.use("/server2", "linecorp.com:80");
            proxy.use("/server3", "localhost:5523");
            try {
                proxy.start();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }).start();

        val server = NIOHttpServer.port(1111);
        server.use(ctx -> {
            System.out.println(ctx.getMethod().toString());
            System.out.println(ctx.getPath());
            return HttpResponse.nextAsync();
        });
        server.get("/", ctx -> HttpResponse.ofAsync("You are at index!"));
        server.get("/path/**", ctx -> HttpResponse.ofAsync(ctx.getPath()));
        server.post("/echo", ctx -> HttpResponse.ofAsync(ctx.getBody()));
        server.start();
    }
}
