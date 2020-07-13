package com.tuhuynh.httpserver.experiments;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class NIOServer {
    public static final String HTTP_HEADER_OK = "HTTP/1.1 200 OK\n\n";
    public static final String HTTP_HEADER_NOT_FOUND = "HTTP/1.1 404\n\n" + "{\"message\": \"404\"}";

    public static void writeData(final SelectionKey key) throws Exception {
        val channel = (SocketChannel) key.channel();

        val currentThread = Thread.currentThread().getName();
        val responseObject = new ResponseObject("OK", currentThread);
        val responseString = HTTP_HEADER_OK + responseObject + '\n';
        val buff = StandardCharsets.ISO_8859_1.encode(responseString);
        channel.write(buff);
        channel.close();
    }

    private final int serverPort;

    private static void readData(final SelectionKey key) throws Exception {
        val buffer = (ByteBuffer) key.attachment();
        val channel = (SocketChannel) key.channel();
        val read = channel.read(buffer);
        if (read <= 0) {
            return;
        }
        val s = new String(buffer.array(), 0, read).trim();
        System.out.println(s);
    }

    public void start() throws Exception {
        try {
            val serverSocketChannel = ServerSocketChannel.open();
            val selector = Selector.open();

            serverSocketChannel.bind(new InetSocketAddress(serverPort));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NIOServer started at port " + serverPort);

            for (; ; ) {
                if (selector.select(100L) == 0) {
                    continue;
                }

                for (final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                     iterator.hasNext(); iterator.remove()) {
                    val key = iterator.next();
                    if (key.isAcceptable()) {
                        val server = (ServerSocketChannel) key.channel();
                        val client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                                        ByteBuffer.allocate(1024));
                        System.out.println("client connected: " + client);
                    }

                    if (key.isReadable()) {
                        readData(key);
                    }
                    if (key.isWritable()) {
                        writeData(key);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @AllArgsConstructor
    public static class ResponseObject {
        private final String status;
        private final String threadName;

        @Override
        public String toString() {
            return "{\"status\":\"" + status + "\",\"thread_name\":\"" + threadName + "\"}";
        }
    }
}
