package com.jinyframework.websocket;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Builder
public class Socket {
    private final WebSocket conn;
    @Getter
    private final List<String> inRoom = new ArrayList<>();
    private final RoomEvent roomEvent;
    @Getter
    @Setter
    private String identify;

    public void join(final String roomName) {
        if (!inRoom.contains(roomName)) {
            inRoom.add(roomName);
            roomEvent.join(conn, roomName);
        }
    }

    public void leave(final String roomName) {
        inRoom.remove(roomName);
        roomEvent.leave(conn, roomName);
    }

    public void emit(final String message) {
        conn.send(message);
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
