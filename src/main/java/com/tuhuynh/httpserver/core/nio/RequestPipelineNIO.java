package com.tuhuynh.httpserver.core.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;

import com.tuhuynh.httpserver.core.RequestBinderBase.BaseHandlerMetadata;
import com.tuhuynh.httpserver.core.RequestBinderBase.RequestHandlerBase;
import com.tuhuynh.httpserver.core.RequestBinderBase.RequestHandlerNIO;
import com.tuhuynh.httpserver.core.RequestUtils;

import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;

public final class RequestPipelineNIO implements ChannelHandlerNIO, RequestHandlerBase {
    private final SocketChannel socketChannel;
    private final Selector selector;
    private final LinkedList<String> messageQueue;
    private final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> handlers;

    public RequestPipelineNIO(final SocketChannel socketChannel, final Selector selector,
                              final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> handlers)
            throws IOException {
        this.socketChannel = socketChannel;
        this.selector = selector;
        this.handlers = handlers;

        messageQueue = new LinkedList<>();

        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ).attach(this);

        selector.wakeup();
    }

    @Override
    public void read() throws Exception {
        val buffer = ByteBuffer.allocate(1024);
        socketChannel.read(buffer);

        val msg = MessageCodec.decode(buffer);
        messageQueue.addLast(msg);

        socketChannel.register(selector, SelectionKey.OP_WRITE).attach(this);
        selector.wakeup();
    }

    @Override
    public void write() throws Exception {
        if (messageQueue.isEmpty()) {
            socketChannel.register(selector, SelectionKey.OP_READ).attach(this);
            return;
        }

        val msg = messageQueue.removeFirst();
        val msgList = msg.split("\n");

        var body = "";
        val requestParts = msg.split("\n\r");
        if (requestParts.length == 2) {
            body = requestParts[1].trim();
        }

        val requestMetadata = RequestUtils.parseRequest(msgList, body);

        val responseObject = new RequestBinderNIO(requestMetadata, handlers).getResponseObject();

        responseObject.thenAccept(responseObjectReturned -> {
            val responseString = RequestUtils.parseResponse(responseObjectReturned);

            try {
                socketChannel.write(MessageCodec.encode(responseString));
                socketChannel.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });
    }

    @NoArgsConstructor
    private static final class MessageCodec {
        static ByteBuffer encode(final String msg) {
            return ByteBuffer.wrap(msg.getBytes());
        }

        static String decode(final ByteBuffer buffer) {
            return new String(buffer.array(), buffer.arrayOffset(), buffer.remaining());
        }
    }
}
