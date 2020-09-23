package com.tuhuynh.jerrymouse.core;

import com.tuhuynh.jerrymouse.core.ParserUtils.RequestMethod;
import lombok.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RequestBinder {
    protected final RequestContext requestContext;

    protected BinderInitObject binderInit(final BaseHandlerMetadata<?> h) {
        val indexOfQuestionMark = requestContext.getPath().indexOf('?');
        var requestPath =
                indexOfQuestionMark == -1 ? requestContext.getPath() : requestContext.getPath().substring(0,
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
                    requestContext.getParam().put(handlerParamKey, handlerParamValue);
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

    public interface RequestHandlerBase {
    }

    @FunctionalInterface
    public interface RequestHandlerBIO extends RequestHandlerBase {
        HttpResponse handleFunc(RequestContext requestContext) throws Exception;
    }

    @FunctionalInterface
    public interface RequestHandlerNIO extends RequestHandlerBase {
        CompletableFuture<HttpResponse> handleFunc(RequestContext requestContext) throws Exception;
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
    public static final class RequestContext {
        private final RequestMethod method;
        private final String path;
        private final HashMap<String, String> header;
        private final String body;
        private final HashMap<String, String> query;
        private final HashMap<String, String> param;
        private final HashMap<String, String> data;

        public void putHandlerData(final String key, final String value) {
            data.put(key, value);
        }

        public String getData(final String key) {
            return data.get(key);
        }
    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    @Getter
    @Setter
    public abstract static class BaseHandlerMetadata<T extends RequestHandlerBase> {
        public RequestMethod method;
        public String path;
        public T[] handlers;
    }

    @Getter
    @Setter
    public static class BIOHandlerMetadata extends BaseHandlerMetadata<RequestHandlerBIO> {
        public BIOHandlerMetadata(final RequestMethod method, final String path,
                                  final RequestHandlerBIO[] handlers) {
            super(method, path, handlers);
        }
    }

    @Getter
    @Setter
    public static class NIOHandlerMetadata extends BaseHandlerMetadata<RequestHandlerNIO> {
        public NIOHandlerMetadata(final RequestMethod method, final String path,
                                  final RequestHandlerNIO[] handlers) {
            super(method, path, handlers);
        }
    }

    @Getter
    public static final class HttpResponse {
        public static HttpResponse next() { return new HttpResponse(0, "", true); }

        public static CompletableFuture<HttpResponse> nextAsync() {
            return CompletableFuture.completedFuture(new HttpResponse(0, "", true));
        }

        public static HttpResponse reject(final String errorText) {
            return new HttpResponse(400, errorText, false);
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
                                                                  final RequestTransformer transformer,
                                                                  final int httpStatusCode) {
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

        private int httpStatusCode;
        private Object responseObject;
        private final boolean allowNext;

        private <T> HttpResponse(final int httpStatusCode, final T responseObject, final boolean allowNext) {
            this.httpStatusCode = httpStatusCode;
            this.responseObject = responseObject;
            this.allowNext = allowNext;
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
