package com.jinyframework;

import com.jinyframework.core.AbstractHttpRouter;
import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.RequestTransformer;
import com.jinyframework.core.bio.RequestPipeline;
import com.jinyframework.core.factories.ServerThreadFactory;
import com.jinyframework.core.utils.Intro;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.*;

/**
 * The type Http server.
 */
@Slf4j
public final class HttpServer extends AbstractHttpRouter<Handler> {
    private final int serverPort;
    private final ServerThreadFactory threadFactory = new ServerThreadFactory("request-processor");
    private String serverHost;
    private ServerSocket serverSocket;
    private int maxThread = Runtime.getRuntime().availableProcessors() * 20;
    private int coreThread = 10;
    private long keepAliveTime = 60L;
    private Executor executor = new ThreadPoolExecutor(coreThread, maxThread,
            keepAliveTime, TimeUnit.SECONDS,
            new SynchronousQueue<>(), threadFactory);


    private HttpServer(final int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Port http server.
     *
     * @param serverPort the server port
     * @return the http server
     */
    public static HttpServer port(final int serverPort) {
        return new HttpServer(serverPort);
    }

    /**
     * Host http server.
     *
     * @param serverHost the server host
     * @return the http server
     */
    public HttpServer host(@NonNull final String serverHost) {
        this.serverHost = serverHost;
        return this;
    }

    /**
     * Use transformer http server.
     *
     * @param transformer the transformer
     * @return the http server
     */
    public HttpServer useTransformer(@NonNull final RequestTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    /**
     * Use response headers http server.
     *
     * @param responseHeaders the response headers
     * @return the http server
     */
    public HttpServer useResponseHeaders(@NonNull final Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
        return this;
    }

    /**
     * Sets thread debug mode.
     *
     * @param isDebug the is debug
     * @return the thread debug mode
     */
    public HttpServer setThreadDebugMode(final boolean isDebug) {
        threadFactory.setDebug(isDebug);
        return this;
    }

    /**
     * Sets num of request threads. Default: available processors * 2
     *
     * @param maxThread the max thread
     * @return the http server
     * @throws IOException the io exception
     */
    public HttpServer setMaxRequestThreads(final int maxThread) throws IOException {
        if (maxThread < 1) {
            throw new ArithmeticException("maxThread cannot lower than 1");
        }
        this.maxThread = maxThread;
        return this;
    }

    /**
     * Sets executor. Default: {@link #executor}
     *
     * @param executor the executor
     * @return the executor
     */
    public HttpServer setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Start.
     */
    public void start() {
        Intro.begin();
        try {
            serverSocket = new ServerSocket();
            val socketAddress =
                    serverHost != null ?
                            new InetSocketAddress(serverHost, serverPort) :
                            new InetSocketAddress(InetAddress.getLoopbackAddress(), serverPort);
            serverSocket.bind(socketAddress);
            log.info("Started Jiny HTTP Server on " + serverPort + " using " + maxThread + " threads at max.");
            while (!Thread.interrupted()) {
                val clientSocket = serverSocket.accept();
                executor.execute(
                        new RequestPipeline(clientSocket, middlewares, handlers, responseHeaders, transformer));
                log.info("Threads: " + Thread.activeCount());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Stop.
     */
    public void stop() {
        if (!serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            log.info("Stopped Jiny HTTP Server on port " + serverPort);
        }
    }
}
