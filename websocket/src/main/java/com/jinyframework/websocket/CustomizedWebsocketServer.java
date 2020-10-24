package com.jinyframework.websocket;

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
    private final CustomizedWebsocketCallback callback;
    private final RoomEvent roomEvent;
    private SocketHandshake socketHandshake;

    public CustomizedWebsocketServer(final int port, final CustomizedWebsocketCallback callback, RoomEvent roomEvent) {
        super(new InetSocketAddress("localhost", port));
        this.callback = callback;
        this.roomEvent = roomEvent;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        callback.onOpen(conn, handshake);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        callback.onClose(conn, code, reason, remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        callback.onMessage(conn, message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        callback.onError(conn, ex);
    }

    @Override
    public void onStart() {
        callback.onStart();
    }

    public void setValidateHandshake(SocketHandshake socketHandshake) {
        this.socketHandshake = socketHandshake;
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        ServerHandshakeBuilder builder = super
                .onWebsocketHandshakeReceivedAsServer(conn, draft, request);
        val socket = Socket.builder().conn(conn).roomEvent(roomEvent).build();
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
}
