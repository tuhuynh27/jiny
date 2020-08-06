package com.tuhuynh.jerrymouse.core.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import com.tuhuynh.jerrymouse.core.ParserUtils;
import com.tuhuynh.jerrymouse.core.RequestBinder.BaseHandlerMetadata;
import com.tuhuynh.jerrymouse.core.RequestBinder.RequestHandlerNIO;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;

public class RequestPipeline {
    private final AsynchronousSocketChannel clientSocketChannel;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private final ArrayList<RequestHandlerNIO> middlewares;
    private final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> handlers;

    public RequestPipeline(final AsynchronousSocketChannel clientSocketChannel,
                           final ArrayList<RequestHandlerNIO> middlewares,
                           final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> handlers)
            throws IOException {
        this.clientSocketChannel = clientSocketChannel;
        this.middlewares = middlewares;
        this.handlers = handlers;
    }

    public void run() throws Exception {
        if (clientSocketChannel != null && clientSocketChannel.isOpen()) {
            read().thenAccept(msg -> {
                try {
                    write(msg);
                } catch (Exception e) {
                    e.getStackTrace();
                }
            });
            byteBuffer.clear();
        } else {
            System.out.println("DCM");
        }
    }

    private CompletableFuture<String> read() {
        CompletableFuture<String> promise = new CompletableFuture<>();

        clientSocketChannel.read(byteBuffer, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                val msg = MessageCodec.decode(byteBuffer);
                byteBuffer.flip();
                promise.complete(msg);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println(exc.getMessage());
            }
        });

        return promise;
    }

    private void write(final String msg) throws Exception {
        String[] req;
        var body = "";
        val requestParts = msg.split("\n\r");
        if (requestParts.length > 0) {
            req = requestParts[0].trim().split("\n");
            body = requestParts.length == 2 ? requestParts[1].trim() : "";

            val requestContext = ParserUtils.parseRequest(req, body);
            val responseObject = new RequestBinderNIO(requestContext, middlewares, handlers)
                    .getResponseObject();

            responseObject.thenAccept(responseObjectReturned -> {
                val responseString = ParserUtils.parseResponse(responseObjectReturned);

                clientSocketChannel.write(MessageCodec.encode(responseString), null,
                                          new CompletionHandler<Integer, Object>() {
                                              @SneakyThrows
                                              @Override
                                              public void completed(Integer result, Object attachment) {
                                                  clientSocketChannel.close();
                                              }

                                              @SneakyThrows
                                              @Override
                                              public void failed(Throwable exc, Object attachment) {
                                                  System.out.println(exc.getMessage());
                                              }
                                          });
            });
        }
    }

    @NoArgsConstructor
    public static final class MessageCodec {
        public static ByteBuffer encode(final String msg) {
            return ByteBuffer.wrap(msg.getBytes());
        }

        public static String decode(final ByteBuffer buffer) {
            return new String(buffer.array(), buffer.arrayOffset(), buffer.remaining());
        }
    }
}
