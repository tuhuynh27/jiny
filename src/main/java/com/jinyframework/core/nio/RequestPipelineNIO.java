package com.jinyframework.core.nio;

import com.jinyframework.core.RequestBinderBase.HandlerMetadata;
import com.jinyframework.core.RequestBinderBase.HandlerNIO;
import com.jinyframework.core.RequestBinderBase.RequestTransformer;
import com.jinyframework.core.RequestPipelineBase;
import com.jinyframework.core.utils.ParserUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
        process().thenAccept(canContinue -> {
            if (canContinue) {
                run();
            }
        });
    }

    private CompletableFuture<Boolean> process() {
        val promise = new CompletableFuture<Boolean>();

        clientSocketChannel.read(byteBuffer, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                val msg = MessageCodec.decode(byteBuffer);
                byteBuffer.flip();

                val requestParts = msg.split("\n\r");
                if (requestParts.length > 0) {
                    val req = requestParts[0].trim().split("\r\n");
                    val body = requestParts.length == 2 ? requestParts[1].trim() : "";

                    val requestContext = ParserUtils.parseRequest(req, body);
                    val response = new RequestBinderNIO(requestContext, middlewares, handlers);
                    response.getResponseObject().thenAccept(responseObjectReturned -> {
                        val responseHeaders = response.getResponseHeaders();
                        val responseString = ParserUtils.parseResponse(responseObjectReturned, responseHeaders, transformer);

                        clientSocketChannel.write(MessageCodec.encode(responseString), null,
                                new CompletionHandler<Integer, Object>() {
                                    @Override
                                    public void completed(Integer result, Object attachment) {
                                        byteBuffer.clear();
                                        promise.complete(true);
                                    }

                                    @SneakyThrows
                                    @Override
                                    public void failed(Throwable e, Object attachment) {
                                        byteBuffer.clear();
                                        clientSocketChannel.close();
                                        promise.complete(false);
                                    }
                                });
                    });
                }
            }

            @SneakyThrows
            @Override
            public void failed(Throwable e, Object attachment) {
                clientSocketChannel.close();
                promise.complete(false);
            }
        });

        return promise;
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
