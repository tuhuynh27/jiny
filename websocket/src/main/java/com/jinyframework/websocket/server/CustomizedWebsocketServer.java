package com.jinyframework.websocket.server;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

@Slf4j
public class CustomizedWebsocketServer extends WebSocketServer {
    private final SocketEventHandler socketEventHandler;
    private final RoomEventHandler roomEventHandler;
    private SocketHandshake socketHandshake;

    public CustomizedWebsocketServer(final int port, final SocketEventHandler socketEventHandler, RoomEventHandler roomEventHandler) {
        super(new InetSocketAddress("localhost", port));
        this.socketEventHandler = socketEventHandler;
        this.roomEventHandler = roomEventHandler;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        socketEventHandler.onOpen(conn, handshake);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        socketEventHandler.onClose(conn, code, reason, remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        socketEventHandler.onMessage(conn, message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        socketEventHandler.onError(conn, ex);
    }

    @Override
    public void onStart() {
        socketEventHandler.onStart();
    }

    public void setValidateHandshake(SocketHandshake socketHandshake) {
        this.socketHandshake = socketHandshake;
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        ServerHandshakeBuilder builder = super
                .onWebsocketHandshakeReceivedAsServer(conn, draft, request);
        val socket = Socket.builder().conn(conn).roomEventHandler(roomEventHandler).build();
        if (socketHandshake != null) {
            try {
                val identify = socketHandshake.handshake(request);
                socket.setIdentify(identify);
            } catch (Exception e) {
                throw new InvalidDataException(CloseFrame.POLICY_VALIDATION, e.getMessage());
            }
        }
        conn.setAttachment(socket);
        return builder;
    }

    @FunctionalInterface
    public interface SocketHandshake {
        String handshake(ClientHandshake request) throws Exception;
    }

    public interface SocketEventHandler {
        void onStart();

        void onOpen(WebSocket conn, ClientHandshake handshake);

        void onClose(WebSocket conn, int code, String reason, boolean remote);

        void onMessage(WebSocket conn, String message);

        void onError(WebSocket conn, Exception ex);
    }

    public interface RoomEventHandler {
        void join(WebSocket conn, String roomName);

        void leave(WebSocket conn, String roomName);
    }
}
