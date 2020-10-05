package com.jinyframework.core.nio;

import com.jinyframework.core.RequestBinderBase.HandlerMetadata;
import com.jinyframework.core.RequestBinderBase.HandlerNIO;
import com.jinyframework.core.RequestBinderBase.RequestTransformer;
import com.jinyframework.core.RequestPipelineBase;
import com.jinyframework.core.utils.ParserUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public final class RequestPipelineNIO implements RequestPipelineBase {
    private final AsynchronousSocketChannel clientSocketChannel;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private final List<HandlerMetadata<HandlerNIO>> middlewares;
    private final List<HandlerMetadata<HandlerNIO>> handlers;

    private final RequestTransformer transformer;

    @Override
    public void run() {
        if (clientSocketChannel != null && clientSocketChannel.isOpen()) {
            read().thenAccept(msg -> {
                try {
                    process(msg);
                }
                catch (IOException ignored) {
                    try {
                        clientSocketChannel.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
            byteBuffer.clear();
        }
    }

    private CompletableFuture<String> read() {
        val promise = new CompletableFuture<String>();

        clientSocketChannel.read(byteBuffer, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                val msg = MessageCodec.decode(byteBuffer);
                byteBuffer.flip();
                promise.complete(msg);
            }

            @Override
            public void failed(Throwable e, Object attachment) {
                promise.completeExceptionally(e);
            }
        });

        return promise;
    }

    private void process(@NonNull final String msg) throws Exception {
        val requestParts = msg.split("\n\r");
        if (requestParts.length > 0) {
            val req = requestParts[0].trim().split("\r\n");
            val body = requestParts.length == 2 ? requestParts[1].trim() : "";

            val requestContext = ParserUtils.parseRequest(req, body);
            val responseObject = new RequestBinderNIO(requestContext, middlewares, handlers)
                    .getResponseObject();

            responseObject.thenAccept(responseObjectReturned -> {
                val responseString = ParserUtils.parseResponse(responseObjectReturned, transformer);

                clientSocketChannel.write(MessageCodec.encode(responseString), null,
                        new CompletionHandler<Integer, Object>() {
                            @SneakyThrows
                            @Override
                            public void completed(Integer result, Object attachment) {
                                if (clientSocketChannel.isOpen()) {
                                    run(); // TODO: Keep-Alive check
                                }
                            }

                            @SneakyThrows
                            @Override
                            public void failed(Throwable ignored, Object attachment) {
                                clientSocketChannel.close();
                            }
                        });
            });
        }
    }

    @UtilityClass
    public final class MessageCodec {
        public ByteBuffer encode(final String msg) {
            return ByteBuffer.wrap(msg.getBytes());
        }

        public String decode(final ByteBuffer buffer) {
            return new String(buffer.array(), buffer.arrayOffset(), buffer.remaining());
        }
    }
}
