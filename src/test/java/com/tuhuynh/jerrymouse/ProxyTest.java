package com.tuhuynh.jerrymouse;

import com.tuhuynh.jerrymouse.core.RequestBinderBase.HttpResponse;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("api.ProxyTest")
public class ProxyTest {
    static final String url = "http://localhost:8000";

    @BeforeAll
    static void startProxy() throws IOException, InterruptedException {
        val serverOne = HttpServer.port(1111);
        serverOne.get("/hello", ctx -> HttpResponse.of("Hello World"));
        new Thread(() -> {
            try {
                serverOne.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        val serverTwo = NIOHttpServer.port(2222);
        serverTwo.get("/bye", ctx -> HttpResponse.ofAsync("Bye"));
        new Thread(() -> {
            try {
                serverTwo.start();
            } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }).start();

        val proxy = Proxy.port(8000);
        proxy.use("/one", "localhost:1111");
        proxy.use("/two", "localhost:2222");
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
                .url(url + "/one/hello").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "Hello World");
    }

    @Test
    @DisplayName("Bye")
    void bye() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/two/bye").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "Bye");
    }

    @BeforeEach
    void each() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
    }
}
