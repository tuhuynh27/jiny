package com.jinyframework.websocket;

@FunctionalInterface
public interface WebsocketCallback {
    void newMessage(Socket socket, String message);
}
