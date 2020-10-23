package com.jinyframework.websocket;

import org.java_websocket.WebSocket;

@FunctionalInterface
public interface CustomizedWebsocketCallback {
    void onMessage(WebSocket conn, String message);
}
