package com.jinyframework.websocket;

import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("api.WebSocketServerTest")
public class WebSocketServerTest {
    private static final WebSocketServer wsServer = WebSocketServer.port(1234);
    private static final CountDownLatch lock = new CountDownLatch(1);
    private static boolean isDone;

    @BeforeAll
    static void beforeAll() {
        wsServer.handshake(req -> {
            val randomId = ThreadLocalRandom.current().nextInt();
            return "guess" + randomId;
        });
        wsServer.onOpen(socket ->
                System.out.println("New connection: " + socket.getIdentify()));
        wsServer.on("ping", (socket, message) -> {
            socket.emit("pong");
        });
        wsServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException, InterruptedException {
        wsServer.stop();
    }

    @Test
    @DisplayName("Ping Pong")
    void ping() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(1000);
        val client = WebSocketClient.builder().uri("ws://localhost:1234").build();
        client.on("pong", message -> {
            isDone = true;
            lock.countDown();
        });
        client.connect();
        TimeUnit.MILLISECONDS.sleep(1000);
        client.emit("ping", "This is ping message and it will be ignored");
        lock.await(5000, TimeUnit.MILLISECONDS);
        assertTrue(isDone);
        client.close();
    }
}
