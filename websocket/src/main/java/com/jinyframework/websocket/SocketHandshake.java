package com.jinyframework.websocket;

import org.java_websocket.handshake.ClientHandshake;

@FunctionalInterface
public interface SocketHandshake {
    String handshake(ClientHandshake request) throws Exception;
}
