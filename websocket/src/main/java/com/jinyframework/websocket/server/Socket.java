package com.jinyframework.websocket.server;

import com.jinyframework.websocket.protocol.ProtocolConstants;
import lombok.*;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Socket.
 */
@Builder
public class Socket {
    private final WebSocket conn;
    @Getter
    private final List<String> inRooms = new ArrayList<>();
    private final CustomizedWebSocketServer.RoomEventHandler roomEventHandler;
    @Getter
    @Setter
    private String identify;

    /**
     * Join.
     *
     * @param roomName the room name
     */
    public void join(final String roomName) {
        if (!inRooms.contains(roomName)) {
            inRooms.add(roomName);
            roomEventHandler.join(conn, roomName);
        }
    }

    /**
     * Leave.
     *
     * @param roomName the room name
     */
    public void leave(final String roomName) {
        inRooms.remove(roomName);
        roomEventHandler.leave(conn, roomName);
    }

    /**
     * Emit.
     *
     * @param message the message
     */
    public void emit(final String message) {
        conn.send(message);
    }

    /**
     * Emit.
     *
     * @param topic   the topic
     * @param message the message
     */
    public void emit(@NonNull final String topic, final String message) {
        val data = topic + ProtocolConstants.DIVIDER + message;
        conn.send(data);
    }

    /**
     * Ping.
     */
    public void ping() {
        conn.sendPing();
    }

    /**
     * Gets local socket address.
     *
     * @return the local socket address
     */
    public InetSocketAddress getLocalSocketAddress() {
        return conn.getLocalSocketAddress();
    }

    /**
     * Gets remote socket address.
     *
     * @return the remote socket address
     */
    public InetSocketAddress getRemoteSocketAddress() {
        return conn.getRemoteSocketAddress();
    }

    /**
     * Close.
     */
    public void close() {
        conn.close();
    }

    /**
     * Force close.
     *
     * @param code    the code
     * @param message the message
     */
    public void forceClose(final int code, final String message) {
        conn.closeConnection(code, message);
    }
}
