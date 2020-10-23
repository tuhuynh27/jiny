package com.jinyframework.websocket;

import org.java_websocket.WebSocket;

@FunctionalInterface
public interface RoomEvent {
    void emit(RoomEventType type, WebSocket conn, String roomName);
}
