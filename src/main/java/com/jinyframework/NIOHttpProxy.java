package com.jinyframework;

import com.jinyframework.core.factories.ServerThreadFactory;
import com.jinyframework.core.nio.RequestPipelineNIO.MessageCodec;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public final class NIOHttpProxy {
    private final int proxyPort;
    private final Map<String, String> endpointMap = new HashMap<>();

    public static NIOHttpProxy port(final int proxyPort) {
        return new NIOHttpProxy(proxyPort);
    }

    public void use(@NonNull final String path, @NonNull final String endpoint) {
        endpointMap.put(path, endpoint);
    }

    public void start() throws IOException {
        val threadFactory = new ServerThreadFactory("proxy-event-loop");
        val group = AsynchronousChannelGroup.withFixedThreadPool(32, threadFactory);
        val serverSocketChannel = AsynchronousServerSocketChannel.open(group);
        serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), proxyPort));
        log.info("Started NIO HTTP Proxy Server on port " + proxyPort + " using " + 32 + " event loop thread(s)");
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @SneakyThrows
            @Override
            public void completed(AsynchronousSocketChannel clientSocketChannel, Object attachment) {
                serverSocketChannel.accept(null, this);
                new ProxyHandler(clientSocketChannel, endpointMap).process();
            }

            @Override
            public void failed(Throwable e, Object attachment) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @RequiredArgsConstructor
    public static final class ProxyHandler {
        private final AsynchronousSocketChannel clientSocketChannel;
        private final Map<String, String> endpointMap;

        public void process() {
            handleSocket().thenAccept(canContinue -> {
                if (canContinue) {
                    process();
                }
            });
        }

        public CompletableFuture<Boolean> handleSocket() {
            val promise = new CompletableFuture<Boolean>();
            val byteBuffer = ByteBuffer.allocate(1024);
            clientSocketChannel.read(byteBuffer, null, new CompletionHandler<Integer, Object>() {
                @SneakyThrows
                @Override
                public void completed(Integer result, Object attachment) {
                    val msg = MessageCodec.decode(byteBuffer);
                    byteBuffer.flip();

                    System.out.println(msg);

                    val msgArr = msg.split("\n");
                    val firstLineArr = msgArr[0].split(" ");
                    val path = firstLineArr[1];

                    var matchedKey = endpointMap.keySet().stream()
                            .filter(path::startsWith).findFirst()
                            .orElse(null);
                    if (matchedKey == null) {
                        if (endpointMap.get("/") != null) {
                            matchedKey = "/";
                        }
                    }

                    if (matchedKey == null) {
                        System.out.println("1");
                        clientSocketChannel.write(MessageCodec.encode("HTTP/1.1 404 Not Found\nContent-Length: 10\n\nNot Found\n\n"), null, new CompletionHandler<Integer, Object>() {
                            @Override
                            public void completed(Integer result, Object attachment) {
                                System.out.println("2");
                                byteBuffer.clear();
                                promise.complete(true);
                            }

                            @SneakyThrows
                            @Override
                            public void failed(Throwable e, Object attachment) {
                                System.out.println("3");
                                log.error(e.getMessage(), e);
                                byteBuffer.clear();
                                clientSocketChannel.close();
                                promise.complete(false);
                            }
                        });
                        System.out.println("4");
                    } else {
                        // Replace path
                        if (path.equals(matchedKey)) {
                            msgArr[0] = msgArr[0].replace(matchedKey, "");
                        }

                        val endpoint = endpointMap.get(matchedKey);
                        val serverMetadata = endpoint.split(":");

                        val serverSocketChannel = AsynchronousSocketChannel.open();
                        serverSocketChannel.connect(new InetSocketAddress(serverMetadata[0], Integer.parseInt(serverMetadata[1])), null, new CompletionHandler<Void, Object>() {
                            @Override
                            public void completed(Void result, Object attachment) {
                                serverSocketChannel.write(MessageCodec.encode(msg), null, new CompletionHandler<Integer, Object>() {
                                    @Override
                                    public void completed(Integer result, Object attachment) {
                                        byteBuffer.clear();

                                        serverSocketChannel.read(byteBuffer, null, new CompletionHandler<Integer, Object>() {
                                            @Override
                                            public void completed(Integer result, Object attachment) {
                                                byteBuffer.flip();

                                                clientSocketChannel.write(byteBuffer, null, new CompletionHandler<Integer, Object>() {
                                                    @Override
                                                    public void completed(Integer result, Object attachment) {
                                                        byteBuffer.clear();
                                                        promise.complete(true);
                                                    }

                                                    @SneakyThrows
                                                    @Override
                                                    public void failed(Throwable e, Object attachment) {
                                                        log.error(e.getMessage(), e);
                                                        byteBuffer.clear();
                                                        clientSocketChannel.close();
                                                        promise.complete(false);
                                                    }
                                                });
                                            }

                                            @SneakyThrows
                                            @Override
                                            public void failed(Throwable e, Object attachment) {
                                                log.error(e.getMessage(), e);
                                                byteBuffer.clear();
                                                serverSocketChannel.close();
                                                promise.complete(false);
                                            }
                                        });
                                    }

                                    @SneakyThrows
                                    @Override
                                    public void failed(Throwable e, Object attachment) {
                                        log.error(e.getMessage(), e);
                                        byteBuffer.clear();
                                        promise.complete(false);
                                    }
                                });
                            }

                            @SneakyThrows
                            @Override
                            public void failed(Throwable e, Object attachment) {
                                log.error(e.getMessage(), e);
                                // Can't connect to target server
                                clientSocketChannel.close();
                                promise.complete(false);
                            }
                        });
                    }
                }

                @SneakyThrows
                @Override
                public void failed(Throwable exc, Object attachment) {
                    log.error(exc.getMessage(), exc);
                    byteBuffer.clear();
                    promise.complete(false);
                }
            });

            return promise;
        }
    }
}
