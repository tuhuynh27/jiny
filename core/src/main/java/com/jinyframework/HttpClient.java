package com.jinyframework;

import lombok.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Builder
public final class HttpClient {
    private final String url;
    private final String method;
    @Singular
    private final Map<String, String> headers;
    private final String body;

    public ResponseObject perform() throws IOException {
        val url = new URL(this.url);
        val conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json; " + StandardCharsets.UTF_8);
        conn.setRequestProperty("Accept", "application/json");
        if (headers != null) {
            for (val header : headers.entrySet()) {
                val key = header.getKey();
                val value = header.getValue();
                if (key != null && value != null) {
                    conn.setRequestProperty(key, value);
                }
            }
        }

        if (body != null && !body.isEmpty()) {
            conn.setDoOutput(true);
            @Cleanup OutputStream os = conn.getOutputStream();
            val input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            os.flush();
        }

        val responseStatus = conn.getResponseCode();
        val responseStatusText = conn.getResponseMessage();
        val isError = responseStatus >= 400;

        val headers = new HashMap<String, String>();
        for (val entry : conn.getHeaderFields().entrySet()) {
            headers.put(entry.getKey(), String.join("; ", entry.getValue()));
        }

        @Cleanup val in = new BufferedReader(
                new InputStreamReader(!isError ? conn.getInputStream() : conn.getErrorStream()));
        val responseStringArr = new ArrayList<String>();
        var decodedString = "";
        while ((decodedString = in.readLine()) != null) {
            responseStringArr.add(decodedString);
        }

        val sb = new StringBuilder();
        for (val s : responseStringArr) {
            sb.append(s);
        }

        conn.disconnect();

        return ResponseObject.builder()
                .status(responseStatus)
                .statusText(responseStatusText)
                .headers(headers)
                .body(sb.toString()).build();
    }

    @Builder
    @Getter
    public static final class ResponseObject {
        private final int status;
        private final String statusText;
        private final Map<String, String> headers;
        private final String body;

        public String getHeader(@NonNull final String key) {
            return headers.get(key) != null ? headers.get(key) : "";
        }
    }
}
