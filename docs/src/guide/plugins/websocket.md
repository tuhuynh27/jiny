# WebSocket

:::warning WIP
This feature is in experimental
:::

WebSockets allow for two-way communication between a client and server. Unlike HTTP, which has a request and response
 pattern, WebSocket peers can send an arbitrary number of messages in either direction. Jiny's WebSocket API allows you
  to create both clients and servers that handle messages asynchronously.
  
## Install

Latest version: ![Maven Central](https://img.shields.io/maven-central/v/com.jinyframework/websocket?style=flat-square)

`build.gradle`

```groovy
dependencies {
    compile group: 'com.jinyframework', name: 'websocket', version: '{latest_version}'
}
```

## Server

WebSocket server can be spawn like this:

```java
import com.jinyframework.websocket.WebSocketServer;

public class Main {
    public static void main(String[] args) {
        val wsServer = WebSocketServer.port(1234);
        wsServer.start();
```

## Messages

The `WebSocketServer` class has methods for sending and receiving messages as well as listening for events. WebSockets can transmit data via text format. Text messages are interpreted as UTF-8 strings.

Message contains two parts: `topic` and `message`.

### Sending

Messages can be sent to all connected WebSocket clients using the WebSocket's emit method:

```java
wsServer.emit('topicA', 'sample message');
```

### Receiving

Incoming messages are handled via the `.on` callbacks.

```java
// Listen on topic 'hello'
io.on("hello", (socket, message) -> {
    socket.emit("hi"); 
    // Only send Hi to the WebSocket client
    // who sent message on "hello" topic
});
```

## Handshake

Define a handshake process for websocket connection:

```java
// Return identify string or reject by throwing exeptions
wsServer.handshake(req -> {
    val token = req.getFieldValue('cookie');
    try {
        val userId = validateToken(token);
        return userId;
    } catch(Exception e) {
        throw e;
    }
});
```

## Room

You can define arbitrary channels called “Rooms” that sockets can join and leave.

This is useful to broadcast data to a subset of sockets:

![](https://socket.io/images/rooms.png)

### Joining and leaving

You can call `join` to subscribe the socket to a given channel:

```java
wsServer.on("room/join", (socket, roomName) -> socket.join(roomName)); // Join room

wsServer.on("room/leave", (socket, roomName) -> socket.leave(roomName)); // Leave room
```

And then simply use `emitRoom` when broadcasting or emitting to rooms:

```java
wsServer.emitRoom('room1', 'topicA', 'sample message');
```

### Sample use cases

#### Broadcast data to each device / tab of a given user

```java
wsServer.onOpen(socket -> {
  val userId = socket.getIdentify();

  socket.join(userId);

  // and then later
  wsServer.emitRoom(userId, 'topicA', 'hi');
});
```

## Client

Sample WebSocket Client usage:

```java
import com.jinyframework.websocket.WebSocketClient;

val client = WebSocketClient.builder()
        .uri("ws://localhost:1234")
        .build().connect();

client.on("chat/global", message -> {
    System.out.println("New global chat: " + message);
});

client.on("pong", message -> {
    System.out.println("Pong: " + message);
});

client.emit("chat/global", "Hello World");
client.emit("ping", "Ping message");
```