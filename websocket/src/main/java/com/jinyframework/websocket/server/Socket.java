package com.jinyframework.websocket.server;

import com.jinyframework.websocket.protocol.ProtocolConstants;
import lombok.*;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Builder
public class Socket {
    private final WebSocket conn;
    @Getter
    private final List<String> inRooms = new ArrayList<>();
    private final CustomizedWebSocketServer.RoomEventHandler roomEventHandler;
    @Getter
    @Setter
    private String identify;

    public void join(final String roomName) {
        if (!inRooms.contains(roomName)) {
            inRooms.add(roomName);
            roomEventHandler.join(conn, roomName);
        }
    }

    public void leave(final String roomName) {
        inRooms.remove(roomName);
        roomEventHandler.leave(conn, roomName);
    }

    public void emit(final String message) {
        conn.send(message);
    }

    public void emit(@NonNull final String topic, final String message) {
        val data = topic + ProtocolConstants.DIVIDER + message;
        conn.send(data);
    }

    public void ping() {
        conn.sendPing();
    }

    public InetSocketAddress getLocalSocketAddress() {
        return conn.getLocalSocketAddress();
    }

    public InetSocketAddress getRemoteSocketAddress() {
        return conn.getRemoteSocketAddress();
    }

    public void close() {
        conn.close();
    }

    public void forceClose(final int code, final String message) {
        conn.closeConnection(code, message);
    }
}
