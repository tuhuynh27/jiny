package com.jinyframework.core.bio;

import com.jinyframework.core.RequestBinderBase.Handler;
import com.jinyframework.core.RequestBinderBase.HandlerMetadata;
import com.jinyframework.core.RequestBinderBase.RequestTransformer;
import com.jinyframework.core.RequestPipelineBase;
import com.jinyframework.core.utils.ParserUtils;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public final class RequestPipeline implements RequestPipelineBase, Runnable {
    private final Socket socket;
    private final List<HandlerMetadata<Handler>> middlewares;
    private final List<HandlerMetadata<Handler>> handlers;
    private final RequestTransformer transformer;

    @SneakyThrows
    @Override
    public void run() {
        @Cleanup val in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        @Cleanup val out = new PrintWriter(socket.getOutputStream(), false);

        val requestStringArr = new ArrayList<String>();
        String inputLine;
        while (!(inputLine = in.readLine()).isEmpty()) {
            requestStringArr.add(inputLine);
        }
        val body = new StringBuilder();
        while (in.ready()) {
            body.append((char) in.read());
        }

        // Log incoming requests
        log.info(String.valueOf(requestStringArr));

        val requestContext = ParserUtils
                .parseRequest(requestStringArr.toArray(new String[0]), body.toString());

        val responseObject = new RequestBinder(requestContext, middlewares, handlers).getResponseObject();
        val responseString = ParserUtils.parseResponse(responseObject, transformer);

        out.write(responseString);

        out.flush();
        socket.close();
    }
}
