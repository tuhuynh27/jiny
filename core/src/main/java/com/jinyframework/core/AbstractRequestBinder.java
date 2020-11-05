package com.jinyframework.core;

import com.jinyframework.core.AbstractRequestBinder.HandlerBase;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractRequestBinder<T extends HandlerBase> {
    protected final Context context;
    protected final List<HandlerMetadata<T>> middlewares;
    protected final List<HandlerMetadata<T>> handlerMetadata;

    protected BinderInitObject binderInit(@NonNull final HandlerMetadata<?> h) {
        val indexOfQuestionMark = context.getPath().indexOf('?');
        var requestPath =
                indexOfQuestionMark == -1 ? context.getPath() : context.getPath().substring(0,
                        indexOfQuestionMark);
        // Remove all last '/' from the requestPath
        while (requestPath.endsWith("/")) {
            requestPath = requestPath.substring(0, requestPath.length() - 1);
        }

        val handlerPathOriginal = h.getPath();
        val handlerPathArr = Arrays.stream(handlerPathOriginal.split("/"));
        val handlerPath = handlerPathArr.filter(e -> !e.startsWith(":")).collect(
                Collectors.joining("/"));

        val numOfHandlerParams = handlerPathOriginal.length() - handlerPathOriginal.replace(":", "")
                .length();
        val numOfSlashOfRequestPath = requestPath.length() - requestPath.replace("/", "").length();
        val numOfSlashOfHandlerPath = handlerPathOriginal.length() - handlerPathOriginal.replace("/", "")
                .length();

        val requestWithHandlerParamsMatched = numOfHandlerParams > 0 && requestPath.startsWith(handlerPath)
                && numOfSlashOfRequestPath == numOfSlashOfHandlerPath;

        val isCatchAll = handlerPath.endsWith("/**");
        val handlerCatchAllPattern = handlerPath.length() > 3 ? handlerPath.substring(0, handlerPath.length()
                - 2) : "";
        val isMatchCatchAll = isCatchAll && (requestPath.isEmpty() ? "/".startsWith(handlerCatchAllPattern) :
                requestPath.startsWith(handlerCatchAllPattern));

        if (requestWithHandlerParamsMatched) {
            val elementsOfHandlerPath = handlerPathOriginal.split("/");
            val elementsOfRequestPath = requestPath.split("/");
            for (int i = 1; i < elementsOfHandlerPath.length; i++) {
                if (elementsOfHandlerPath[i].startsWith(":")) {
                    val handlerParamKey = elementsOfHandlerPath[i].replace(":", "");
                    val handlerParamValue = elementsOfRequestPath[i];
                    context.getParam().put(handlerParamKey, handlerParamValue);
                }
            }
        }

        return BinderInitObject.builder()
                .requestPath(requestPath)
                .handlerPath(handlerPath)
                .requestWithHandlerParamsMatched(requestWithHandlerParamsMatched)
                .isMatchCatchAll(isMatchCatchAll)
                .build();
    }

    public Map<String, String> getResponseHeaders(final Map<String, String> defaultResponseHeaders) {
        context.getResponseHeaders().putAll(defaultResponseHeaders);
        return context.getResponseHeaders();
    }

    public interface HandlerBase {
    }

    @FunctionalInterface
    public interface Handler extends HandlerBase {
        HttpResponse handleFunc(Context context) throws Exception;
    }

    @FunctionalInterface
    public interface HandlerNIO extends HandlerBase {
        CompletableFuture<HttpResponse> handleFunc(Context context) throws Exception;
    }

    @FunctionalInterface
    public interface RequestTransformer {
        String render(Object model);
    }

    @Builder
    @Getter
    protected static class BinderInitObject {
        String requestPath;
        String handlerPath;
        boolean requestWithHandlerParamsMatched;
        boolean isMatchCatchAll;
    }

    @Getter
    @Builder
    public static final class Context {
        private final HttpMethod method;
        private final String path;
        private final Map<String, String> header;
        private final String body;
        private final Map<String, String> query;
        private final Map<String, String> param;
        private final Map<String, Object> data;

        private final Map<String, String> responseHeaders;

        public String headerParam(@NonNull final String name) {
            return header.get(name.toLowerCase()) != null ? header.get(name.toLowerCase()) : "";
        }

        public String pathParam(@NonNull final String name) {
            return param.get(name) != null ? param.get(name) : "";
        }

        public String queryParam(@NonNull final String name) {
            return query.get(name) != null ? query.get(name) : "";
        }

        public Object dataParam(@NonNull final String name) {
            return data.get(name) != null ? data.get(name) : "";
        }

        public void setDataParam(@NonNull final String key, @NonNull final Object value) {
            data.put(key, value);
        }

        public void putHeader(@NonNull final String key, @NonNull final String value) {
            responseHeaders.put(key, value);
        }
    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    @Getter
    @Setter
    public static class HandlerMetadata<T extends HandlerBase> {
        public HttpMethod method;
        public String path;
        public T[] handlers;
    }

    @Getter
    public static final class HttpResponse {
        private final boolean allowNext;
        private int httpStatusCode;
        private Object responseObject;

        private <T> HttpResponse(final int httpStatusCode, final T responseObject, final boolean allowNext) {
            this.httpStatusCode = httpStatusCode;
            this.responseObject = responseObject;
            this.allowNext = allowNext;
        }

        public static HttpResponse next() {
            return new HttpResponse(0, "", true);
        }

        public static CompletableFuture<HttpResponse> nextAsync() {
            return CompletableFuture.completedFuture(new HttpResponse(0, "", true));
        }

        public static HttpResponse reject(final String errorText) {
            return new HttpResponse(400, errorText, false);
        }

        public static CompletableFuture<HttpResponse> rejectAsync(final String errorText) {
            return CompletableFuture.completedFuture(new HttpResponse(400, errorText, false));
        }

        public static CompletableFuture<HttpResponse> rejectAsync(final String errorText,
                                                                  final int httpStatusCode) {
            return CompletableFuture.completedFuture(new HttpResponse(httpStatusCode, errorText, false));
        }

        public static CompletableFuture<HttpResponse> of(
                final CompletableFuture<HttpResponse> completableFuture) {
            return completableFuture;
        }

        public static <T> CompletableFuture<HttpResponse> ofAsync(final T t) {
            return CompletableFuture.completedFuture(of(t));
        }

        public static <T> CompletableFuture<HttpResponse> ofAsync(final T t, final int httpStatusCode) {
            return CompletableFuture.completedFuture(of(t).status(httpStatusCode));
        }

        public static <T> CompletableFuture<HttpResponse> ofAsync(final T t,
                                                                  final RequestTransformer transformer) {
            return CompletableFuture.completedFuture(of(t).transform(transformer).status(200));
        }

        public static <T> CompletableFuture<HttpResponse> ofAsync(final T t,
                                                                  final int httpStatusCode,
                                                                  final RequestTransformer transformer) {
            return CompletableFuture.completedFuture(of(t).transform(transformer).status(httpStatusCode));
        }

        public static <T> HttpResponse of(final T t) {
            return new HttpResponse(200, t, true);
        }

        public static <T> HttpResponse of(final T t, final int httpStatusCode) {
            return new HttpResponse(httpStatusCode, t, true);
        }

        public static CompletableFuture<HttpResponse> createPromise() {
            return new CompletableFuture<>();
        }

        public HttpResponse transform(RequestTransformer transformer) {
            responseObject = transformer.render(responseObject);
            return this;
        }

        public HttpResponse status(final int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }
    }
}
