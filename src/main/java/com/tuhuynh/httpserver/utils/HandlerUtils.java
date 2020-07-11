package com.tuhuynh.httpserver.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor
public final class HandlerUtils {
    private static final Pattern HEADER_PATTERN = Pattern.compile(": ");

    public static RequestContext parseRequest(final ArrayList<String> request, final String payload) {
        val metaArr = request.get(0).split(" ");

        val header = new HashMap<String, String>();
        for (int i = 1; i < request.size(); i++) {
            val headerLine = request.get(i);
            val headerArr = HEADER_PATTERN.split(headerLine);
            header.put(headerArr[0], headerArr[1]);
        }

        return RequestContext.builder()
                             .method(getMethod(metaArr[0]))
                             .path(metaArr[1].toLowerCase())
                             .header(header)
                             .payload(payload)
                             .build();
    }

    private static RequestMethod getMethod(final String originalMethod) {
        val method = originalMethod.toLowerCase();
        switch (method) {
            case "post":
                return RequestMethod.POST;
            case "put":
                return RequestMethod.PUT;
            case "delete":
                return RequestMethod.DELETE;
            default:
                return RequestMethod.GET;
        }
    }

    public enum RequestMethod {
        GET,
        POST,
        PUT,
        DELETE
    }

    @Data
    @Getter
    @Builder
    public static final class RequestContext {
        private RequestMethod method;
        private String path;
        private HashMap<String, String> header;
        private String payload;
    }
}
