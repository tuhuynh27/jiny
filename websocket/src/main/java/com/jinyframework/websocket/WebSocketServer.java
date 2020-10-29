package com.jinyframework.websocket;

import com.jinyframework.core.utils.Intro;
import com.jinyframework.websocket.protocol.ProtocolConstants;
import com.jinyframework.websocket.server.CustomizedWebSocketServer;
import com.jinyframework.websocket.server.Socket;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Web socket server.
 */
@Slf4j
public final class WebSocketServer {
    private final CustomizedWebSocketServer customizedWebsocketServer;
    private final Map<String, Collection<WebSocket>> rooms = new HashMap<>();
    private final Map<String, NewMessageHandler> callbackHashMap = new HashMap<>();
    private ConnOpenHandler connOpenHandler;
    private ConnCloseHandler connCloseHandler;
    private OnErrorHandler onErrorHandler;

    private WebSocketServer(final int port) {
        customizedWebsocketServer = new CustomizedWebSocketServer(port, new CustomizedWebSocketServer.SocketEventHandler() {
            @Override
            public void onStart() {
                Intro.begin();
                log.info("Started Jiny Websocket Server on port " + port);
            }

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                val socket = (Socket) conn.getAttachment();
                connOpenHandler.handle(socket);
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                val socket = (Socket) conn.getAttachment();
                connCloseHandler.handle(socket, code, reason);
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                val messageArray = message.split(ProtocolConstants.DIVIDER);
                val socket = (Socket) conn.getAttachment();
                if (messageArray.length >= 1) {
                    val topic = messageArray[0];
                    val data = message.replaceFirst(topic + ProtocolConstants.DIVIDER, "");
                    val callback = callbackHashMap.get(topic);
                    if (callback != null) {
                        callback.handle(socket, data);
                    }
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                if (conn == null || conn.getAttachment() == null) {
                    return;
                }

                val socket = (Socket) conn.getAttachment();
                if (socket != null) {
                    onErrorHandler.handle(socket, ex);
                }
            }
        }, new CustomizedWebSocketServer.RoomEventHandler() {
            @Override
            public void join(WebSocket conn, String roomName) {
                val isRoomEmpty = rooms.get(roomName) == null;
                val r = isRoomEmpty ? new ArrayList<WebSocket>() : rooms.get(roomName);
                r.add(conn);
                if (isRoomEmpty) {
                    rooms.put(roomName, r);
                }
            }

            @Override
            public void leave(WebSocket conn, String roomName) {
                val isRoomEmpty = rooms.get(roomName) == null;
                if (!isRoomEmpty) {
                    val r = rooms.get(roomName);
                    r.remove(conn);
                }
            }
        });
    }

    /**
     * Port web socket server.
     *
     * @param port the port
     * @return the web socket server
     */
    public static WebSocketServer port(final int port) {
        return new WebSocketServer(port);
    }

    /**
     * Start.
     */
    public void start() {
        customizedWebsocketServer.start();
    }

    /**
     * Stop.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    public void stop() throws IOException, InterruptedException {
        customizedWebsocketServer.stop();
    }

    /**
     * Handshake.
     *
     * @param socketHandshake the socket handshake
     */
    public void handshake(final CustomizedWebSocketServer.SocketHandshake socketHandshake) {
        customizedWebsocketServer.setValidateHandshake(socketHandshake);
    }

    /**
     * On open.
     *
     * @param callback the callback
     */
    public void onOpen(final ConnOpenHandler callback) {
        connOpenHandler = callback;
    }

    /**
     * On close.
     *
     * @param callback the callback
     */
    public void onClose(final ConnCloseHandler callback) {
        connCloseHandler = callback;
    }

    /**
     * On error.
     *
     * @param callback the callback
     */
    public void onError(final OnErrorHandler callback) {
        onErrorHandler = callback;
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
     * Emit.
     *
     * @param topic    the topic
     * @param messages the messages
     */
    public void emit(@NonNull final String topic, final String... messages) {
        val messageData = String.join(ProtocolConstants.DIVIDER, messages);
        customizedWebsocketServer.broadcast(topic + ProtocolConstants.DIVIDER + messageData);
    }

    /**
     * Emit room.
     *
     * @param roomName the room name
     * @param topic    the topic
     * @param messages the messages
     */
    public void emitRoom(@NonNull final String roomName, @NonNull final String topic, final String... messages) {
        val target = rooms.get(roomName);
        if (target != null) {
            val messageData = String.join(ProtocolConstants.DIVIDER, messages);
            customizedWebsocketServer.broadcast(topic + ProtocolConstants.DIVIDER + messageData, target);
        }
    }

    /**
     * The interface New message handler.
     */
    @FunctionalInterface
    public interface NewMessageHandler {
        /**
         * Handle.
         *
         * @param socket  the socket
         * @param message the message
         */
        void handle(Socket socket, String message);
    }

    /**
     * The interface Conn open handler.
     */
    @FunctionalInterface
    public interface ConnOpenHandler {
        /**
         * Handle.
         *
         * @param socket the socket
         */
        void handle(Socket socket);
    }

    /**
     * The interface Conn close handler.
     */
    @FunctionalInterface
    public interface ConnCloseHandler {
        /**
         * Handle.
         *
         * @param socket the socket
         * @param code   the code
         * @param reason the reason
         */
        void handle(Socket socket, int code, String reason);
    }

    /**
     * The interface On error handler.
     */
    @FunctionalInterface
    public interface OnErrorHandler {
        /**
         * Handle.
         *
         * @param socket the socket
         * @param ex     the ex
         */
        void handle(Socket socket, Exception ex);
    }
}
