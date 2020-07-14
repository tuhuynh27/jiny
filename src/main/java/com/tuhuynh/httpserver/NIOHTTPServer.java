package com.tuhuynh.httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.tuhuynh.httpserver.core.RequestUtils.RequestMethod;
import com.tuhuynh.httpserver.nio.ChannelHandlerNIO;
import com.tuhuynh.httpserver.nio.RequestBinderNIO.HandlerMetadata;
import com.tuhuynh.httpserver.nio.RequestBinderNIO.RequestHandler;
import com.tuhuynh.httpserver.nio.RequestHandlerNIO;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public final class NIOHTTPServer implements Runnable {
    public static NIOHTTPServer port(final int serverPort) throws IOException {
        val reactor = new NIOHTTPServer();
        reactor.serverSocket.bind(new InetSocketAddress(serverPort));
        return reactor;
    }

    private final Selector selector;
    private final Executor eventLoop;
    private final ServerSocketChannel serverSocket;
    private ArrayList<HandlerMetadata> handlers = new ArrayList<>();

    private NIOHTTPServer() throws IOException {
        selector = Selector.open();
        eventLoop = Executors.newSingleThreadExecutor(runnable -> {
            val thread = new Thread(runnable);
            thread.setName("NIOEventLoop");
            return thread;
        });
        serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT).attach(
                new ServerAcceptor(serverSocket, selector, handlers));
    }

    private static void dispatch(final SelectionKey selectionKey) {
        val handler = (ChannelHandlerNIO) selectionKey.attachment();
        try {
            if (selectionKey.isReadable() || selectionKey.isAcceptable()) {
                handler.read();
            } else if (selectionKey.isWritable()) {
                handler.write();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void addHandler(final RequestMethod method, final String path,
                           final RequestHandler... handlers) {
        val newHandlers = HandlerMetadata.builder().method(method).path(path).handlers(handlers).build();
        this.handlers.add(newHandlers);
    }

    public void use(final String path, final RequestHandler... handlers) {
        val newHandlers = HandlerMetadata.builder().method(RequestMethod.ALL).path(path).handlers(handlers)
                                         .build();
        this.handlers.add(newHandlers);
    }

    public void get(final String path, final RequestHandler... handlers) {
        val newHandlers = HandlerMetadata.builder().method(RequestMethod.GET).path(path).handlers(handlers)
                                         .build();
        this.handlers.add(newHandlers);
    }

    public void post(final String path, final RequestHandler... handlers) {
        val newHandlers = HandlerMetadata.builder().method(RequestMethod.POST).path(path).handlers(handlers)
                                         .build();
        this.handlers.add(newHandlers);
    }

    public void put(final String path, final RequestHandler... handlers) {
        val newHandlers = HandlerMetadata.builder().method(RequestMethod.PUT).path(path).handlers(handlers)
                                         .build();
        this.handlers.add(newHandlers);
    }

    public void delete(final String path, final RequestHandler... handlers) {
        val newHandlers = HandlerMetadata.builder().method(RequestMethod.DELETE).path(path).handlers(handlers)
                                         .build();
        this.handlers.add(newHandlers);
    }

    public void start() {
        eventLoop.execute(this);
    }

    @Override
    public void run() {
        for (; ; ) {
            try {
                if (selector.select(100L) == 0) {
                    continue;
                }

                val selectionKeys = selector.selectedKeys();
                selectionKeys.forEach(NIOHTTPServer::dispatch);
                selectionKeys.clear();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @RequiredArgsConstructor
    private static class ServerAcceptor implements ChannelHandlerNIO {
        private final ServerSocketChannel serverSocket;
        private final Selector selector;
        private final ArrayList<HandlerMetadata> handlers;

        @Override
        public void read() throws Exception {
            new RequestHandlerNIO(serverSocket.accept(), selector, handlers);
        }

        @Override
        public void write() throws Exception {
            throw new UnsupportedOperationException();
        }
    }
}
