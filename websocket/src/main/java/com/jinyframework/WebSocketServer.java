package com.jinyframework;

import com.jinyframework.core.utils.Intro;
import com.jinyframework.websocket.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WebSocketServer {
    private final CustomizedWebsocketServer customizedWebsocketServer;
    private final Map<String, Collection<WebSocket>> rooms = new HashMap<>();
    private final Map<String, NewMessageHandler> callbackHashMap = new HashMap<>();
    private ConnOpenHandler connOpenHandler;
    private ConnCloseHandler connCloseHandler;
    private OnErrorHandler onErrorHandler;

    private WebSocketServer(final int port) {
        customizedWebsocketServer = new CustomizedWebsocketServer(port, new CustomizedWebsocketCallback() {
            @Override
            public void onStart() {
                Intro.begin();
                log.info("Started Jiny Websocket Server on port " + port);
            }

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                val socket = (Socket) conn.getAttachment();
                connOpenHandler.handle(socket);
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                val socket = (Socket) conn.getAttachment();
                connCloseHandler.handle(socket, code, reason);
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                val messageArray = message.split(":");
                val socket = (Socket) conn.getAttachment();
                if (messageArray.length >= 1) {
                    val topic = messageArray[0];
                    val data = message.replaceFirst(topic + ":", "");
                    val callback = callbackHashMap.get(topic);
                    if (callback != null) {
                        callback.handle(socket, data);
                    }
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                val socket = (Socket) conn.getAttachment();
                onErrorHandler.handle(socket, ex);
            }
        }, new RoomEvent() {
            @Override
            public void join(WebSocket conn, String roomName) {
                val isRoomEmpty = rooms.get(roomName) == null;
                val r = isRoomEmpty ? new ArrayList<WebSocket>() : rooms.get(roomName);
                r.add(conn);
                if (isRoomEmpty) {
                    rooms.put(roomName, r);
                }
            }

            @Override
            public void leave(WebSocket conn, String roomName) {
                val isRoomEmpty = rooms.get(roomName) == null;
                if (!isRoomEmpty) {
                    val r = rooms.get(roomName);
                    r.remove(conn);
                }
            }
        });
    }

    public static WebSocketServer port(final int port) {
        return new WebSocketServer(port);
    }

    public void start() {
        customizedWebsocketServer.start();
    }

    public void stop() throws IOException, InterruptedException {
        customizedWebsocketServer.stop();
    }

    public void handshake(final SocketHandshake socketHandshake) {
        customizedWebsocketServer.setValidateHandshake(socketHandshake);
    }

    public void onOpen(final ConnOpenHandler callback) {
        connOpenHandler = callback;
    }

    public void onClose(final ConnCloseHandler callback) {
        connCloseHandler = callback;
    }

    public void onError(final OnErrorHandler callback) {
        onErrorHandler = callback;
    }

    public void on(final String topic, final NewMessageHandler callback) {
        callbackHashMap.put(topic, callback);
    }

    public void emit(final String topic, final String... messages) {
        val messageData = String.join(":", messages);
        customizedWebsocketServer.broadcast(topic + ":" + messageData);
    }

    public void emitRoom(final String roomName, final String topic, final String... messages) {
        val target = rooms.get(roomName);
        if (target != null) {
            val messageData = String.join(":", messages);
            customizedWebsocketServer.broadcast(topic + ":" + messageData, target);
        }
    }

    @FunctionalInterface
    public interface NewMessageHandler {
        void handle(Socket socket, String message);
    }

    @FunctionalInterface
    public interface ConnOpenHandler {
        void handle(Socket socket);
    }

    @FunctionalInterface
    public interface ConnCloseHandler {
        void handle(Socket socket, int code, String reason);
    }

    @FunctionalInterface
    public interface OnErrorHandler {
        void handle(Socket socket, Exception ex);
    }
}
