package com.jinyframework.core.bio;

import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.HandlerMetadata;
import com.jinyframework.core.AbstractRequestBinder.RequestTransformer;
import com.jinyframework.core.utils.ParserUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

@Slf4j
@RequiredArgsConstructor
public final class RequestPipeline implements Runnable {
    private final Socket socket;
    private final List<HandlerMetadata<Handler>> middlewares;
    private final List<HandlerMetadata<Handler>> handlers;
    private final Map<String, String> responseHeaders;
    private final RequestTransformer transformer;
    private BufferedReader in;
    private PrintWriter out;

    @SneakyThrows
    @Override
    public void run() {
        socket.setSoTimeout(5000);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), false);

        boolean canContinue = true;
        while (canContinue) {
            canContinue = process();
        }

        in.close();
        out.close();
        socket.close();
    }

    public boolean process() throws IOException {
        val requestStringArr = new ArrayList<String>();
        String inputLine = "";
        boolean isFirstLine = true;
        String method = "";
        int contentLength = 0;
        try {
            while (!socket.isClosed()) {
                inputLine = in.readLine();
                if (inputLine == null || inputLine.isEmpty()) {
                    break;
                }
                if (isFirstLine) {
                    isFirstLine = false;
                    method = new StringTokenizer(inputLine).nextToken().toLowerCase();
                }
                if (inputLine.toLowerCase().startsWith("content-length: ")) {
                    val contentLengthStr = inputLine.toLowerCase().replace("content-length: ", "");
                    contentLength = Integer.parseInt(contentLengthStr);
                }
                requestStringArr.add(inputLine);
            }

            if (requestStringArr.size() == 0) {
                return false;
            }

            val body = new StringBuilder();
            if (!method.equals("get") && contentLength > 0) {
                while (in.ready()) {
                    body.append((char) in.read());
                }
            }

            val requestContext = ParserUtils
                    .parseRequest(requestStringArr.toArray(new String[0]), body.toString().trim());

            val response = new RequestBinder(requestContext, middlewares, handlers);
            val responseHeaders = response.getResponseHeaders(this.responseHeaders);
            val responseObject = response.getResponseObject();
            val responseString = ParserUtils.parseResponse(responseObject, responseHeaders, transformer);

            out.write(responseString);
            out.flush();

            return true;
        } catch (SocketException | SocketTimeoutException ignored) {
            socket.close();
            return false;
        } catch (Exception e) {
            socket.close();
            log.error(e.getMessage(), e);
            return false;
        }
    }
}
