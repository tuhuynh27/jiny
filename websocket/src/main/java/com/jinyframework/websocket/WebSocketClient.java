package com.jinyframework.websocket;

import com.jinyframework.websocket.client.CustomizedWebsocketClient;
import com.jinyframework.websocket.protocol.Constants;
import lombok.*;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Builder
public class WebSocketClient {
    private static final Map<String, NewMessageHandler> callbackHashMap = new HashMap<>();
    private static CustomizedWebsocketClient customizedWebsocketClient;
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

        val uriConverted = new URI(uri);

        customizedWebsocketClient = new CustomizedWebsocketClient(uriConverted, headers, new CustomizedWebsocketClient.SocketEventHandler() {
            @Override
            public void onOpen(ServerHandshake handshakeData) throws InterruptedException {
                connOpenHandler.handle(handshakeData);
            }

            @Override
            public void onMessage(String message) {
                val messageArray = message.split(Constants.PROTOCOL_MESSAGE_DIVIDER);
                if (messageArray.length >= 1) {
                    val topic = messageArray[0];
                    val data = message.replaceFirst(topic + Constants.PROTOCOL_MESSAGE_DIVIDER, "");
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

    public void emit(final String topic, final String... messages) {
        val data = String.join(Constants.PROTOCOL_MESSAGE_DIVIDER, messages);
        customizedWebsocketClient.send(topic + Constants.PROTOCOL_MESSAGE_DIVIDER + data);
    }

    public void on(@NonNull final String topic, final NewMessageHandler callback) {
        callbackHashMap.put(topic, callback);
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
