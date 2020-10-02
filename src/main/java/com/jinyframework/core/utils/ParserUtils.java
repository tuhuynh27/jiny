package com.jinyframework.core.utils;

import com.jinyframework.core.RequestBinderBase.HttpResponse;
import com.jinyframework.core.RequestBinderBase.RequestContext;
import com.jinyframework.core.RequestBinderBase.RequestTransformer;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@UtilityClass
public final class ParserUtils {
    private final Pattern HEADER_PATTERN = Pattern.compile(": ");

    public RequestContext parseRequest(@NonNull final String[] request, @NonNull final String body) {
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

        return RequestContext.builder()
                .method(getMethod(metaArr[0]))
                .path(path)
                .header(header)
                .body(body)
                .query(splitQuery(queryParamsString))
                .param(new HashMap<>())
                .data(new HashMap<>())
                .build();
    }

    public String parseResponse(@NonNull final HttpResponse httpResponse, @NonNull RequestTransformer transformer) {
        var httpStatusText = "UNKNOWN";
        switch (httpResponse.getHttpStatusCode()) {
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

        return "HTTP/1.1 "
                + httpResponse.getHttpStatusCode() + ' '
                + httpStatusText + "\n\n"
                + transformer.render(httpResponse.getResponseObject()) + '\n';
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

    private SimpleImmutableEntry<String, String> splitQueryParameter(@NonNull String it) {
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
