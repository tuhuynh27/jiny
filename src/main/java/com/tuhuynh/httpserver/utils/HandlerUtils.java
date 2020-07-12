package com.tuhuynh.httpserver.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.tuhuynh.httpserver.handlers.HandlerBinder.HttpResponse;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;

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

        val path = metaArr[1].toLowerCase();
        val indexOfQuestionMark = path.indexOf('?');
        val queryParamsString = indexOfQuestionMark == -1 ? null : path.substring(indexOfQuestionMark + 1);

        return RequestContext.builder()
                             .method(getMethod(metaArr[0]))
                             .path(path)
                             .header(header)
                             .payload(payload)
                             .queryParams(splitQuery(queryParamsString))
                             .build();
    }

    public static String parseResponse(final HttpResponse httpResponse) {
        var httpStatusText = "";
        switch (httpResponse.getHttpStatusCode()) {
            case 200:
                httpStatusText = "OK";
                break;
            case 400:
                httpStatusText = "BAD REQUEST";
                break;
            case 401:
                httpStatusText = "UNAUTHORIZED";
                break;
            case 403:
                httpStatusText = "FORBIDDEN";
                break;
            case 404:
                httpStatusText = "NOT FOUND";
                break;
            case 500:
                httpStatusText = "INTERNAL SERVER ERROR";
                break;
        }

        return "HTTP/1.1 " + httpResponse.getHttpStatusCode() + ' ' + httpStatusText + "\n\n" + httpResponse
                .getResponseString() + '\n';
    }

    public static HashMap<String, String> splitQuery(final String url) {
        if (url == null || url.isEmpty()) {
            return new HashMap<>();
        }
        val urlArr = url.split("&");
        val hashMap = new HashMap<String, String>();
        for (val urlElement : urlArr) {
            val keyVal = splitQueryParameter(urlElement);
            hashMap.put(keyVal.getKey(), keyVal.getValue());
        }
        return hashMap;
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

    private static SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        val idx = it.indexOf('=');
        val key = idx > 0 ? it.substring(0, idx) : it;
        val value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : "";
        try {
            return new SimpleImmutableEntry<>(
                    URLDecoder.decode(key, "UTF-8"),
                    URLDecoder.decode(value, "UTF-8")
            );
        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex.getMessage());
            return new SimpleImmutableEntry<>("", "");
        }
    }

    public enum RequestMethod {
        GET,
        POST,
        PUT,
        DELETE
    }

    @Getter
    @Builder
    public static final class RequestContext {
        private RequestMethod method;
        private String path;
        private HashMap<String, String> header;
        private String payload;
        private HashMap<String, String> queryParams;
        private HashMap<String, String> handlerData;
    }
}
