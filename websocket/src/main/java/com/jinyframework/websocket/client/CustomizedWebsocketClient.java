package com.jinyframework.websocket.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

public class CustomizedWebsocketClient extends WebSocketClient {
    private final SocketEventHandler callback;

    public CustomizedWebsocketClient(URI serverUri, Map<String, String> headers, SocketEventHandler socketEventHandler) {
        super(serverUri);
        callback = socketEventHandler;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        callback.onOpen(handshakeData);
    }

    @Override
    public void onMessage(String message) {
        callback.onMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        callback.onClose(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        if (ex != null && ex.getMessage() != null) {
            callback.onError(ex);
        }
    }

    public interface SocketEventHandler {
        void onOpen(ServerHandshake handshakeData);

        void onMessage(String message);

        void onClose(int code, String reason, boolean remote);

        void onError(Exception ex);
    }
}
