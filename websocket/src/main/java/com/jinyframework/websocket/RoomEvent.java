package com.jinyframework.websocket;

import org.java_websocket.WebSocket;

public interface RoomEvent {
    void join(WebSocket conn, String roomName);

    void leave(WebSocket conn, String roomName);
}
