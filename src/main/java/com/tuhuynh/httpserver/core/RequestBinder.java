package com.tuhuynh.httpserver.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.tuhuynh.httpserver.core.RequestUtils.RequestMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;

@RequiredArgsConstructor
public final class RequestBinder {
    private final RequestContext requestContext;
    private final ArrayList<HandlerMetadata> handlerMetadata;

    public HttpResponse getResponseObject() throws IOException {
        for (val h : handlerMetadata) {
            val indexOfQuestionMark = requestContext.getPath().indexOf('?');
            var requestPath =
                    indexOfQuestionMark == -1 ? requestContext.getPath() : requestContext.getPath().substring(0,
                                                                                                              indexOfQuestionMark);
            // Remove all last '/' from the requestPath
            while (requestPath.endsWith("/")) {
                requestPath = requestPath.substring(0, requestPath.length() - 1);
            }

            val handlerPathOriginal = h.getPath();
            val handlerPathArrWithHandlerParams = Arrays.stream(handlerPathOriginal.split("/"));
            val handlerPath = handlerPathArrWithHandlerParams.filter(e -> !e.startsWith(":")).collect(
                    Collectors.joining("/"));

            val numOfHandlerParams = handlerPathOriginal.length() - handlerPathOriginal.replace(":", "")
                                                                                       .length();
            val numOfSlashOfRequestPath = requestPath.length() - requestPath.replace("/", "").length();
            val numOfSlashOfHandlerPath = handlerPathOriginal.length() - handlerPathOriginal.replace("/", "")
                                                                                            .length();

            val requestWithHandlerParamsMatched = numOfHandlerParams > 0 && requestPath.startsWith(handlerPath)
                                                  && numOfSlashOfRequestPath == numOfSlashOfHandlerPath;

            if (requestWithHandlerParamsMatched) {
                val elementsOfHandlerPath = handlerPathOriginal.split("/");
                val elementsOfRequestPath = requestPath.split("/");
                for (int i = 1; i < elementsOfHandlerPath.length; i++) {
                    if (elementsOfHandlerPath[i].startsWith(":")) {
                        val handlerParamKey = elementsOfHandlerPath[i].replace(":", "");
                        val handlerParamValue = elementsOfRequestPath[i];
                        requestContext.getHandlerParams().put(handlerParamKey, handlerParamValue);
                    }
                }
            }

            if ((requestContext.getMethod() == h.getMethod() || (h.getMethod() == RequestMethod.ALL))
                && (requestPath.equals(handlerPath) || requestWithHandlerParamsMatched)) {
                try {
                    if (h.handlers.length == 1) {
                        return h.handlers[0].handleFunc(requestContext);
                    }

                    // Handle middleware function chain
                    for (int i = 0; i < h.handlers.length; i++) {
                        val isLastItem = i == h.handlers.length - 1;
                        val resultFromPreviousHandler = h.handlers[i].handleFunc(requestContext);
                        if (!isLastItem && !resultFromPreviousHandler.isAllowNext()) {
                            return resultFromPreviousHandler;
                        } else {
                            if (isLastItem) {
                                return resultFromPreviousHandler;
                            } else {
                                continue;
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    return HttpResponse.of("Internal Server Error").status(500);
                }
            }
        }

        return HttpResponse.of("Not found").status(404);
    }

    @FunctionalInterface
    public interface RequestHandler {
        HttpResponse handleFunc(RequestContext requestMetadata) throws Exception;
    }

    @Getter
    @Builder
    public static final class RequestContext {
        private RequestMethod method;
        private String path;
        private HashMap<String, String> header;
        private String body;
        private HashMap<String, String> queryParams;
        private HashMap<String, String> handlerParams;
        private HashMap<String, String> handlerData;

        public void putHandlerData(final String key, final String value) {
            handlerData.put(key, value);
        }

        public String getHandlerData(final String key) {
            return handlerData.get(key);
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static final class HandlerMetadata {
        private RequestMethod method;
        private String path;
        private RequestHandler[] handlers;
    }

    @Getter
    public static final class HttpResponse {
        public static HttpResponse of(final String text) {
            return new HttpResponse(200, text, true);
        }

        public static HttpResponse next() { return new HttpResponse(0, "", true); }

        public static HttpResponse reject(final String errorText) {
            return new HttpResponse(400, errorText, false);
        }

        private int httpStatusCode;
        private String responseString;
        private boolean allowNext;

        private HttpResponse(final int httpStatusCode, final String responseString, final boolean allowNext) {
            this.httpStatusCode = httpStatusCode;
            this.responseString = responseString;
            this.allowNext = allowNext;
        }

        public <T> HttpResponse of(T t) {
            return new HttpResponse(200, t.toString(), true);
        }

        public HttpResponse status(final int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }
    }

}
