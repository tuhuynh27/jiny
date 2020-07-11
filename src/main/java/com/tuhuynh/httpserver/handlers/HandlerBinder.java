package com.tuhuynh.httpserver.handlers;

import java.io.IOException;
import java.util.ArrayList;

import com.tuhuynh.httpserver.utils.HandlerUtils.RequestContext;
import com.tuhuynh.httpserver.utils.HandlerUtils.RequestMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public final class HandlerBinder {
    private final RequestContext requestContext;
    private final ArrayList<HandlerMetadata> handlerMetadata;

    public String getResponseString() throws IOException {
        for (val h : handlerMetadata) {
            if (requestContext.getMethod() == h.getMethod() && requestContext.getPath().equals(
                    h.getPath())) {
                return h.handler.handle(requestContext);
            }
        }

        return "";
    }

    @FunctionalInterface
    public interface RequestHandler {
        String handle(RequestContext requestMetadata) throws IOException;
    }

    @Data
    @Getter
    @Builder
    @AllArgsConstructor
    public static final class HandlerMetadata {
        private RequestMethod method;
        private String path;
        private RequestHandler handler;
    }
}
