package com.tuhuynh.jerrymouse.core.bio;

import com.tuhuynh.jerrymouse.core.RequestBinderBase.HandlerMetadata;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.Handler;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.RequestTransformer;
import com.tuhuynh.jerrymouse.core.utils.ParserUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

@RequiredArgsConstructor
public final class RequestPipeline implements Runnable {
    private final Socket socket;
    private final ArrayList<HandlerMetadata<Handler>> middlewares;
    private final ArrayList<HandlerMetadata<Handler>> handlers;
    private final RequestTransformer transformer;
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
        val body = new StringBuilder();
        while (in.ready()) {
            body.append((char) in.read());
        }

        val requestContext = ParserUtils.parseRequest(requestStringArr.toArray(new String[0]),
                body.toString());

        val responseObject = new RequestBinder(requestContext, middlewares, handlers).getResponseObject();
        val responseString = ParserUtils.parseResponse(responseObject, transformer);

        out.write(responseString);
    }

    public void clean() throws IOException {
        out.flush();
        socket.close();
    }
}
