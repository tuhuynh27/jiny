package com.tuhuynh.httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.tuhuynh.httpserver.core.ChannelHandlerNIO;
import com.tuhuynh.httpserver.core.RequestBinder;
import com.tuhuynh.httpserver.core.RequestBinder.BaseHandlerMetadata;
import com.tuhuynh.httpserver.core.RequestBinder.NIOHandlerMetadata;
import com.tuhuynh.httpserver.core.RequestBinder.RequestHandlerNIO;
import com.tuhuynh.httpserver.core.RequestPipelineNIO;
import com.tuhuynh.httpserver.core.RequestUtils.RequestMethod;

import lombok.RequiredArgsConstructor;
import lombok.val;

public final class NIOHTTPServer implements Runnable {
    public static NIOHTTPServer port(final int serverPort) {
        return new NIOHTTPServer(serverPort);
    }

    private final int serverPort;
    private Selector selector;
    private ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> handlers = new ArrayList<>();

    private NIOHTTPServer(final int serverPort) {
        this.serverPort = serverPort;
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
                           final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(method, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void use(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(RequestMethod.ALL, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void get(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(RequestMethod.GET, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void post(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(RequestMethod.POST, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void put(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(RequestMethod.PUT, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void delete(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(RequestMethod.DELETE, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void start() throws IOException {
        selector = Selector.open();
        Executor eventLoop = Executors.newSingleThreadExecutor(runnable -> {
            val thread = new Thread(runnable);
            thread.setName("NIOEventLoop");
            return thread;
        });
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(serverPort));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT).attach(
                new ServerAcceptor(serverSocket, selector, handlers));
        System.out.println("Started NIO HTTP Server on port " + serverPort);
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
        private final ArrayList<BaseHandlerMetadata<RequestBinder.RequestHandlerNIO>> handlers;

        @Override
        public void read() throws Exception {
            new RequestPipelineNIO(serverSocket.accept(), selector, handlers);
        }

        @Override
        public void write() throws Exception {
            throw new UnsupportedOperationException();
        }
    }
}
