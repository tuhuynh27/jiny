package com.jinyframework;

import com.jinyframework.core.utils.Intro;
import com.jinyframework.websocket.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WebSocketServer {
    private final int port;
    private final CustomizedWebsocketServer customizedWebsocketServer;
    private final Map<String, WebsocketCallback> callbackHashMap = new HashMap<>();
    private final Map<String, Collection<WebSocket>> rooms = new HashMap<>();

    private WebSocketServer(final int port) {
        this.port = port;
        customizedWebsocketServer = new CustomizedWebsocketServer(port, this::processMessage, this::handleRoomEvents);
    }

    public static WebSocketServer port(final int port) {
        return new WebSocketServer(port);
    }

    public void start() {
        Intro.begin();
        log.info("Started Jiny Websocket Server on port " + port);
        customizedWebsocketServer.start();
    }

    public void stop() throws IOException, InterruptedException {
        customizedWebsocketServer.stop();
    }

    public void handshake(final SocketHandshake socketHandshake) {
        customizedWebsocketServer.setValidateHandshake(socketHandshake);
    }

    public void on(final String topic, final WebsocketCallback callback) {
        callbackHashMap.put(topic, callback);
    }

    public void emit(final String topic, final String message) {
        customizedWebsocketServer.broadcast(topic + ":" + message);
    }

    public void emit(final String topic, final String message, final String roomName) {
        val target = rooms.get(roomName);
        if (target != null) {
            customizedWebsocketServer.broadcast(topic + ":" + message, target);
        }
    }

    private void handleRoomEvents(final RoomEventType type, final WebSocket conn, final String roomName) {
        val isRoomEmpty = rooms.get(roomName) == null;
        if (type == RoomEventType.JOIN) {
            val r = isRoomEmpty ? new ArrayList<WebSocket>() : rooms.get(roomName);
            r.add(conn);
            if (isRoomEmpty) {
                rooms.put(roomName, r);
            }
            return;
        }
        if (type == RoomEventType.LEAVE && !isRoomEmpty) {
            val r = rooms.get(roomName);
            r.remove(conn);
        }
    }

    private void processMessage(final WebSocket conn, final String message) {
        val messageArray = message.split(":");
        val socketIdentify = (Socket) conn.getAttachment();
        if (messageArray.length >= 2) {
            val topic = messageArray[0];
            val data = message.replaceFirst(topic + ":", "");
            val callback = callbackHashMap.get(topic);
            if (callback != null) {
                callback.newMessage(socketIdentify, data);
            }
        }
    }
}
