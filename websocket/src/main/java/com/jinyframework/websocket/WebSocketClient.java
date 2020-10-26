package com.jinyframework.websocket;

import com.jinyframework.websocket.client.CustomizedWebSocketClient;
import com.jinyframework.websocket.protocol.ProtocolConstants;
import lombok.*;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Builder
public final class WebSocketClient {
    private static final Map<String, NewMessageHandler> callbackHashMap = new HashMap<>();
    private static CustomizedWebSocketClient customizedWebsocketClient;
    private static ConnOpenHandler connOpenHandler;
    private static ConnCloseHandler connCloseHandler;
    private static OnErrorHandler onErrorHandler;
    private final String uri;
    @Singular
    private Map<String, String> headers;

    @SneakyThrows
    public WebSocketClient connect() {
        if (headers == null) {
            headers = new HashMap<>();
        }

        customizedWebsocketClient = new CustomizedWebSocketClient(new URI(uri), headers, new CustomizedWebSocketClient.SocketEventHandler() {
            @Override
            public void onOpen(ServerHandshake handshakeData) throws InterruptedException {
                connOpenHandler.handle(handshakeData);
            }

            @Override
            public void onMessage(String message) {
                val messageArray = message.split(ProtocolConstants.DIVIDER);
                if (messageArray.length >= 1) {
                    val topic = messageArray[0];
                    val data = message.replaceFirst(topic + ProtocolConstants.DIVIDER, "");
                    val callback = callbackHashMap.get(topic);
                    if (callback != null) {
                        callback.handle(data);
                    }
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                connCloseHandler.handle(code, reason, remote);
            }

            @Override
            public void onError(Exception ex) {
                onErrorHandler.handle(ex);
            }
        });

        customizedWebsocketClient.connect();

        return this;
    }

    public void close() {
        customizedWebsocketClient.close();
    }

    public void emit(@NonNull final String topic, final String... messages) {
        val data = String.join(ProtocolConstants.DIVIDER, messages);
        customizedWebsocketClient.send(topic + ProtocolConstants.DIVIDER + data);
    }

    public void on(@NonNull final String topic, final NewMessageHandler callback) {
        callbackHashMap.put(topic, callback);
    }

    public void onOpen(@NonNull final ConnOpenHandler callback) {
        connOpenHandler = callback;
    }

    public void onClose(@NonNull final ConnCloseHandler callback) {
        connCloseHandler = callback;
    }

    public void onError(@NonNull final OnErrorHandler callback) {
        onErrorHandler = callback;
    }

    @FunctionalInterface
    public interface NewMessageHandler {
        void handle(String message);
    }

    @FunctionalInterface
    public interface ConnOpenHandler {
        void handle(ServerHandshake handshakeData) throws InterruptedException;
    }

    @FunctionalInterface
    public interface ConnCloseHandler {
        void handle(int code, String reason, boolean remote);
    }

    @FunctionalInterface
    public interface OnErrorHandler {
        void handle(Exception ex);
    }
}
