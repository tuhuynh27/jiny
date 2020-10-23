package com.jinyframework;

import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.core.factories.ServerThreadFactory;
import com.jinyframework.core.utils.Intro;
import com.jinyframework.core.utils.MessageCodec;
import com.jinyframework.core.utils.ParserUtils;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
public final class HttpProxy {
    private final int proxyPort;
    private final Map<String, String> endpointMap = new HashMap<>();

    private HttpProxy(final int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public static HttpProxy port(final int proxyPort) {
        return new HttpProxy(proxyPort);
    }

    public void use(@NonNull final String path, @NonNull final String endpoint) {
        endpointMap.put(path, endpoint);
    }

    public void start() throws IOException {
        Intro.begin();
        val threadFactory = new ServerThreadFactory("proxy-event-loop");
        val maxThread = Runtime.getRuntime().availableProcessors() * 2;
        val group = AsynchronousChannelGroup
                .withFixedThreadPool(maxThread, threadFactory);
        val serverSocketChannel = AsynchronousServerSocketChannel.open(group);
        serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), proxyPort));
        log.info("Started NIO HTTP Proxy Server on port " + proxyPort + " using " + maxThread + " event loop thread(s)");
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @SneakyThrows
            @Override
            public void completed(AsynchronousSocketChannel clientSocketChannel, Object attachment) {
                serverSocketChannel.accept(null, this);
                new ProxyHandler(group, clientSocketChannel, endpointMap).process();
            }

            @Override
            public void failed(Throwable e, Object attachment) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @RequiredArgsConstructor
    private static final class ProxyHandler {
        private final AsynchronousChannelGroup group;
        private final AsynchronousSocketChannel clientSocketChannel;
        private final Map<String, String> endpointMap;

        public String createResponse(@NonNull final String text, final int code) {
            val httpResponse = HttpResponse.of(text).status(code);
            return ParserUtils.parseResponse(httpResponse, new HashMap<>(), t -> (String) t);
        }

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

                    val msgArr = msg.split("\n");
                    val firstLineArr = msgArr[0].split(" ");
                    val path = firstLineArr[1];

                    val endpoints = endpointMap.keySet().toArray(new String[0]);
                    val endpointsMatched = new ArrayList<String>();
                    var matchedKey = "";
                    for (val endpoint : endpoints) {
                        if (path.startsWith(endpoint)) {
                            val exactTestStr = path.replace(endpoint, "");
                            if (exactTestStr.startsWith("/") || exactTestStr.length() == 0) {
                                endpointsMatched.add(endpoint);
                            }
                        }
                    }
                    if (endpointsMatched.size() > 0) {
                        matchedKey = Collections.max(endpointsMatched, Comparator.comparing(String::length));
                    }

                    if (matchedKey.isEmpty() && endpointMap.get("/") != null) {
                        matchedKey = "/";
                    }

                    if (matchedKey.isEmpty()) {
                        clientSocketChannel.write(MessageCodec.encode(createResponse("Not found", 404)), null, new CompletionHandler<Integer, Object>() {
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
                    } else {
                        // Replace path
                        msgArr[0] = msgArr[0].replace(matchedKey, !path.equals(matchedKey) ? "" : "/");

                        val endpoint = endpointMap.get(matchedKey);
                        val serverMetadata = endpoint.split(":");

                        val serverSocketChannel = AsynchronousSocketChannel.open(group);
                        serverSocketChannel.connect(new InetSocketAddress(serverMetadata[0], Integer.parseInt(serverMetadata[1])), null, new CompletionHandler<Void, Object>() {
                            @Override
                            public void completed(Void result, Object attachment) {
                                serverSocketChannel.write(MessageCodec.encode(String.join("\n", msgArr)), null, new CompletionHandler<Integer, Object>() {
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
                                clientSocketChannel.write(MessageCodec.encode(createResponse(e.getMessage(), 500)), null, new CompletionHandler<Integer, Object>() {
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
                                promise.complete(true);
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
