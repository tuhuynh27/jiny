package com.jinyframework.websocket;

import com.jinyframework.websocket.protocol.ProtocolConstants;
import com.jinyframework.websocket.server.Socket;
import lombok.val;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("api.WebSocketServerTest")
public class WebSocketServerTest {
    private static final WebSocketServer wsServer = WebSocketServer.port(1234);
    private static final WebSocketClient wsClient = WebSocketClient.builder().uri("ws://localhost:1234").build();
    private static CountDownLatch lock;
    private static String expectedValue;

    @BeforeAll
    static void beforeAll() throws InterruptedException {
        wsServer.handshake(req -> "guess");

        wsServer.on("ping", (socket, message) -> {
            socket.emit("pong", "Pong message");
        });

        wsServer.on("room/join", Socket::join); // Join room
        wsServer.on("room/leave", Socket::leave); // Leave room

        wsServer.on("chat/public", (socket, message) -> {
            wsServer.emit("chat/public", socket.getIdentify(), message);
        });

        // Receive room chat message
        wsServer.on("chat/private", (socket, message) -> {
            val messageArray = message.split(ProtocolConstants.DIVIDER);
            val roomName = messageArray[0];
            val messageData = message.replaceFirst(roomName + ProtocolConstants.DIVIDER, "");
            wsServer.emitRoom(roomName,
                    "chat/private",
                    roomName, socket.getIdentify(), messageData);
        });

        wsServer.start();
        TimeUnit.MILLISECONDS.sleep(1000);
        wsClient.connect();

        lock = new CountDownLatch(1);
        wsClient.onOpen(handshakeData -> {
            lock.countDown();
        });
        lock.await(3000, TimeUnit.MILLISECONDS);
    }

    @AfterAll
    static void afterAll() throws IOException, InterruptedException {
        wsClient.close();
        wsServer.stop();
    }

    @BeforeEach
    void beforeEach() {
        lock = new CountDownLatch(1);
        expectedValue = "";
    }

    @Test
    @DisplayName("Ping Pong")
    void ping() throws InterruptedException {
        wsClient.on("pong", message -> {
            expectedValue = message;
            lock.countDown();
        });

        wsClient.emit("ping", "This is ping message and it will be ignored");

        lock.await(3000, TimeUnit.MILLISECONDS);
        assertEquals(expectedValue, "Pong message");
    }

    @Test
    @DisplayName("Global")
    void global() throws InterruptedException {
        wsClient.on("chat/public", message -> {
            expectedValue = message;
            lock.countDown();
        });

        val msg = "Public chat message";
        wsClient.emit("chat/public", msg);

        lock.await(3000, TimeUnit.MILLISECONDS);
        assertEquals(expectedValue, "guess" + ProtocolConstants.DIVIDER + msg);
    }

    @Test
    @DisplayName("Room")
    void room() throws InterruptedException {
        wsClient.on("chat/private", message -> {
            expectedValue = message;
            lock.countDown();
        });

        val msg = "Hello test-room";
        wsClient.emit("room/join", "test-room");
        wsClient.emit("chat/private", "test-room", msg);

        lock.await(3000, TimeUnit.MILLISECONDS);
        assertEquals(expectedValue, "test-room" + ProtocolConstants.DIVIDER + "guess" + ProtocolConstants.DIVIDER + msg);
    }
}
