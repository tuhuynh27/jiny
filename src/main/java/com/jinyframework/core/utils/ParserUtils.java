package com.jinyframework.core.utils;

import com.jinyframework.core.AbstractRequestBinder.Context;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.core.AbstractRequestBinder.RequestTransformer;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

@Slf4j
@UtilityClass
public final class ParserUtils {
    private final Pattern HEADER_PATTERN = Pattern.compile(": ");

    public Context parseRequest(@NonNull final String[] request, @NonNull final String body) {
        if (request.length == 0) {
            throw new ArithmeticException("Request is empty");
        }

        val metaArr = request[0].split(" ");

        val header = new HashMap<String, String>();
        for (int i = 1; i < request.length; i++) {
            val headerLine = request[i];
            val headerArr = HEADER_PATTERN.split(headerLine);
            if (headerArr.length == 2) {
                val headerKey = headerArr[0];
                val headerValue = headerArr[1];
                if (headerKey != null && headerValue != null) {
                    header.put(headerKey.toLowerCase(), headerValue);
                }
            }
        }

        val path = metaArr.length >= 2 ? metaArr[1].toLowerCase() : "";

        val indexOfQuestionMark = path.indexOf('?');
        val queryParamsString = indexOfQuestionMark == -1 ? null : path.substring(indexOfQuestionMark + 1);

        return Context.builder()
                .method(getMethod(metaArr[0]))
                .path(path)
                .header(header)
                .body(body)
                .query(splitQuery(queryParamsString))
                .param(new HashMap<>())
                .data(new HashMap<>())
                .responseHeaders(new HashMap<>())
                .build();
    }

    public String parseResponse(@NonNull final HttpResponse httpResponse, @NonNull final Map<String, String> responseHeaders, @NonNull RequestTransformer transformer) {
        val body = transformer.render(httpResponse.getResponseObject());

        val dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        val date = new Date();

        val headers = new LinkedHashMap<String, String>();
        headers.put("Content-Type", "text/plain; charset=utf-8");
        headers.put("Content-Length", String.valueOf(body.length() + 1));
        headers.put("Date", dateFormat.format(date));
        headers.put("Server", "Jiny");
        headers.put("Connection", "Keep-Alive");
        headers.putAll(responseHeaders);

        return httpResponseStringBuilder(httpResponse.getHttpStatusCode(), headers, body);
    }

    public String httpResponseStringBuilder(final int statusCode, @NonNull final Map<String, String> headers, final String body) {
        var httpStatusText = "";
        switch (statusCode) {
            case 200:
                httpStatusText = "OK";
                break;
            case 400:
                httpStatusText = "Bad Request";
                break;
            case 401:
                httpStatusText = "Unauthorized";
                break;
            case 403:
                httpStatusText = "Forbidden";
                break;
            case 404:
                httpStatusText = "Not Found";
                break;
            case 500:
                httpStatusText = "Internal Server Error";
                break;
        }

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("HTTP/1.1 ").append(statusCode).append(" ").append(httpStatusText).append("\n");
        for (val header : headers.entrySet()) {
            stringBuilder.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\n");
        }
        stringBuilder.append("\n").append(body).append("\n");
        return stringBuilder.toString();
    }

    public Map<String, String> splitQuery(final String url) {
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

    private HttpMethod getMethod(@NonNull final String originalMethod) {
        val method = originalMethod.toLowerCase();
        switch (method) {
            case "head":
                return HttpMethod.HEAD;
            case "options":
                return HttpMethod.OPTIONS;
            case "get":
                return HttpMethod.GET;
            case "post":
                return HttpMethod.POST;
            case "put":
                return HttpMethod.PUT;
            case "patch":
                return HttpMethod.PATCH;
            case "delete":
                return HttpMethod.DELETE;
            default:
                return HttpMethod.ALL;
        }
    }

    private Entry<String, String> splitQueryParameter(@NonNull String it) {
        val idx = it.indexOf('=');
        val key = idx > 0 ? it.substring(0, idx) : it;
        val value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : "";
        try {
            return new SimpleImmutableEntry<>(
                    URLDecoder.decode(key, "UTF-8"),
                    URLDecoder.decode(value, "UTF-8")
            );
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return new SimpleImmutableEntry<>("", "");
        }
    }

    public enum HttpMethod {
        HEAD,
        OPTIONS,
        GET,
        POST,
        PUT,
        PATCH,
        DELETE,
        ALL
    }
}
