package com.tuhuynh.jerrymouse;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

@RequiredArgsConstructor
@Builder
public final class HttpClient {
    private final String url;
    private final String method;
    private final HashMap<String, String> headers;
    private final String body;

    public ResponseObject perform() throws IOException {
        URL url = new URL(this.url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json; " + StandardCharsets.UTF_8);
        conn.setRequestProperty("Accept", "application/json");
        if (headers != null) {
            for (val header : headers.entrySet()) {
                String key = header.getKey();
                String value = header.getValue();
                conn.setRequestProperty(key, value);
            }
        }

        if (body != null && !body.isEmpty()) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        val responseStatus = conn.getResponseCode();
        val isError = responseStatus >= 400;

        val in = new BufferedReader(
                new InputStreamReader(!isError ? conn.getInputStream() : conn.getErrorStream()));
        val responseStringArr = new ArrayList<String>();
        String decodedString;
        while ((decodedString = in.readLine()) != null) {
            responseStringArr.add(decodedString);
        }
        in.close();

        val sb = new StringBuilder();
        for (val s : responseStringArr) {
            sb.append(s);
        }

        return ResponseObject.builder().status(responseStatus).body(sb.toString()).build();
    }

    @Builder
    @Getter
    public static final class ResponseObject {
        private final String body;
        private final int status;
    }
}
