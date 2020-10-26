package com.jinyframework.websocket;

import com.jinyframework.websocket.protocol.Constants;
import com.jinyframework.websocket.server.Socket;
import lombok.val;
import org.java_websocket.protocols.Protocol;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("api.WebSocketServerTest")
public class WebSocketServerTest {
    private static final WebSocketServer wsServer = WebSocketServer.port(1234);
    private static final WebSocketClient wsClient = WebSocketClient.builder().uri("ws://localhost:1234").build();;
    private static CountDownLatch lock;
    private static boolean isDone;

    @BeforeAll
    static void beforeAll() throws InterruptedException {
        wsServer.handshake(req -> {
            val randomId = ThreadLocalRandom.current().nextInt();
            return "guess" + randomId;
        });
        wsServer.onOpen(socket ->
                System.out.println("New connection: " + socket.getIdentify()));
        wsServer.on("ping", (socket, message) -> {
            socket.emit("pong");
        });

        wsServer.on("room/join", Socket::join); // Join room
        wsServer.on("room/leave", Socket::leave); // Leave room

        // Receive room chat message
        wsServer.on("chat/private", (socket, message) -> {
            val messageArray = message.split(":");
            val roomName = messageArray[0];
            val messageData = message.replaceFirst(roomName + ":", "");
            wsServer.emitRoom(roomName,
                    "chat/private",
                    roomName, socket.getIdentify(), messageData);
        });

        wsServer.start();
        TimeUnit.MILLISECONDS.sleep(1000);
        wsClient.connect();
    }

    @AfterAll
    static void afterAll() throws IOException, InterruptedException {
        wsClient.close();
        wsServer.stop();
    }

    @BeforeEach
    void beforeEach() throws InterruptedException {
        lock = new CountDownLatch(1);
        isDone = false;
    }

    @Test
    @DisplayName("Ping Pong")
    void ping() throws InterruptedException {
        wsClient.onOpen(handshakeData -> {
            wsClient.emit("ping", "This is ping message and it will be ignored");
        });

        wsClient.on("pong", message -> {
            isDone = true;
            lock.countDown();
        });

        lock.await(3000, TimeUnit.MILLISECONDS);
        assertTrue(isDone);
    }

    @Test
    @DisplayName("Room")
    void room() throws InterruptedException {
        wsClient.on("chat/private", message -> {
            isDone = true;
            lock.countDown();
        });

        wsClient.emit("room/join", "test-room");
        wsClient.emit("chat/private", "test-room" + Constants.PROTOCOL_MESSAGE_DIVIDER  + "Hello test-room");

        lock.await(3000, TimeUnit.MILLISECONDS);
        assertTrue(isDone);
    }
}
