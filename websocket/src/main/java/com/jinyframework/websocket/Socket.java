package com.jinyframework.websocket;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.List;

@Builder
public class Socket {
    @Getter
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
            roomEvent.emit(RoomEventType.JOIN, conn, roomName);
        }
    }

    public void leave(final String roomName) {
        inRoom.remove(roomName);
        roomEvent.emit(RoomEventType.LEAVE, conn, roomName);
    }
}
