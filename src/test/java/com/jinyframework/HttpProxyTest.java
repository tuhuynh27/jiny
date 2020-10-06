package com.jinyframework;

import com.jinyframework.core.RequestBinderBase.HttpResponse;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("api.HttpProxyTest")
public class HttpProxyTest {
    static final String url = "http://localhost:8000";

    @BeforeAll
    static void startProxy() throws InterruptedException {
        val serverBIO = HttpServer.port(1111);
        serverBIO.get("/hello", ctx -> HttpResponse.of("Hello World"));
        new Thread(() -> {
            try {
                serverBIO.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        val serverNIO = NIOHttpServer.port(2222);
        serverNIO.get("/bye", ctx -> HttpResponse.ofAsync("Bye"));
        serverNIO.post("/echo", ctx -> HttpResponse.ofAsync(ctx.getBody()));
        new Thread(() -> {
            try {
                serverNIO.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        val proxy = HttpProxy.port(8000);
        proxy.use("/bio", "localhost:1111");
        proxy.use("/nio", "localhost:2222");
        new Thread(() -> {
            try {
                proxy.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Wait for server to start
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    @DisplayName("Hello World")
    void helloWorld() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/bio/hello").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "Hello World");
    }

    // Todo: Re-add the unit test when complete the Proxy Mode

    @BeforeEach
    void each() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(500);
    }
}
