package com.tuhuynh.httpserver.handlers;

import java.io.IOException;
import java.util.ArrayList;

import com.tuhuynh.httpserver.utils.HandlerUtils.RequestContext;
import com.tuhuynh.httpserver.utils.HandlerUtils.RequestMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public final class HandlerBinder {
    private final RequestContext requestContext;
    private final ArrayList<HandlerMetadata> handlerMetadata;

    public HttpResponse getResponseObject() throws IOException {
        for (val h : handlerMetadata) {
            if (requestContext.getMethod() == h.getMethod() && requestContext.getPath().equals(
                    h.getPath())) {
                return h.handler.handle(requestContext);
            }
        }

        return HttpResponse.of("Not found").status(404);
    }

    @FunctionalInterface
    public interface RequestHandler {
        HttpResponse handle(RequestContext requestMetadata) throws IOException;
    }

    @Getter
    public static final class HttpResponse {
        public static HttpResponse of(final String text) {
            return new HttpResponse(200, text);
        }
        private int httpStatusCode;
        private String responseString;

        private HttpResponse(final int httpStatusCode, final String responseString) {
            this.httpStatusCode = httpStatusCode;
            this.responseString = responseString;
        }

        public <T> HttpResponse of(T t) {
            return new HttpResponse(200, t.toString());
        }

        public HttpResponse status(final int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static final class HandlerMetadata {
        private RequestMethod method;
        private String path;
        private RequestHandler handler;
    }
}
