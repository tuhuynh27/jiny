package com.jinyframework.websocket;

import com.jinyframework.websocket.client.CustomizedWebSocketClient;
import com.jinyframework.websocket.protocol.ProtocolConstants;
import lombok.*;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Web socket client.
 */
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

    /**
     * Connect web socket client.
     *
     * @return the web socket client
     */
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

    /**
     * Close.
     */
    public void close() {
        customizedWebsocketClient.close();
    }

    /**
     * Emit.
     *
     * @param topic    the topic
     * @param messages the messages
     */
    public void emit(@NonNull final String topic, final String... messages) {
        val data = String.join(ProtocolConstants.DIVIDER, messages);
        customizedWebsocketClient.send(topic + ProtocolConstants.DIVIDER + data);
    }

    /**
     * On.
     *
     * @param topic    the topic
     * @param callback the callback
     */
    public void on(@NonNull final String topic, final NewMessageHandler callback) {
        callbackHashMap.put(topic, callback);
    }

    /**
     * On open.
     *
     * @param callback the callback
     */
    public void onOpen(@NonNull final ConnOpenHandler callback) {
        connOpenHandler = callback;
    }

    /**
     * On close.
     *
     * @param callback the callback
     */
    public void onClose(@NonNull final ConnCloseHandler callback) {
        connCloseHandler = callback;
    }

    /**
     * On error.
     *
     * @param callback the callback
     */
    public void onError(@NonNull final OnErrorHandler callback) {
        onErrorHandler = callback;
    }

    /**
     * The interface New message handler.
     */
    @FunctionalInterface
    public interface NewMessageHandler {
        /**
         * Handle.
         *
         * @param message the message
         */
        void handle(String message);
    }

    /**
     * The interface Conn open handler.
     */
    @FunctionalInterface
    public interface ConnOpenHandler {
        /**
         * Handle.
         *
         * @param handshakeData the handshake data
         * @throws InterruptedException the interrupted exception
         */
        void handle(ServerHandshake handshakeData) throws InterruptedException;
    }

    /**
     * The interface Conn close handler.
     */
    @FunctionalInterface
    public interface ConnCloseHandler {
        /**
         * Handle.
         *
         * @param code   the code
         * @param reason the reason
         * @param remote the remote
         */
        void handle(int code, String reason, boolean remote);
    }

    /**
     * The interface On error handler.
     */
    @FunctionalInterface
    public interface OnErrorHandler {
        /**
         * Handle.
         *
         * @param ex the ex
         */
        void handle(Exception ex);
    }
}
