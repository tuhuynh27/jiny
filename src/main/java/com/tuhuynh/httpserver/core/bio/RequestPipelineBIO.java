package com.tuhuynh.httpserver.core.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import com.tuhuynh.httpserver.core.RequestBinder.BaseHandlerMetadata;
import com.tuhuynh.httpserver.core.RequestBinder.RequestHandlerBIO;
import com.tuhuynh.httpserver.core.RequestUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor
public final class RequestPipelineBIO implements Runnable {
    private final Socket socket;
    private final ArrayList<RequestHandlerBIO> middlewares;
    private final ArrayList<BaseHandlerMetadata<RequestHandlerBIO>> handlers;
    private BufferedReader in;
    private PrintWriter out;

    @SneakyThrows
    @Override
    public void run() {
        init();
        process();
        clean();
    }

    private void init() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), false);
    }

    private void process() throws IOException {
        val requestStringArr = new ArrayList<String>();
        String inputLine;
        while (!(inputLine = in.readLine()).isEmpty()) {
            requestStringArr.add(inputLine);
        }
        System.out.println(requestStringArr);
        val body = new StringBuilder();
        while (in.ready()) {
            body.append((char) in.read());
        }

        val requestMetadata = RequestUtils.parseRequest(requestStringArr.stream().toArray(String[]::new),
                                                        body.toString());

        val responseObject = new RequestBinderBIO(requestMetadata, middlewares, handlers).getResponseObject();
        val responseString = RequestUtils.parseResponse(responseObject);

        out.write(responseString);
    }

    public void clean() throws IOException {
        out.flush();
        socket.close();
    }
}
