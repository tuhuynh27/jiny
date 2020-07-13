package com.tuhuynh.httpserver.nio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.tuhuynh.httpserver.core.RequestBinder.HttpResponse;
import com.tuhuynh.httpserver.core.RequestBinder.RequestContext;
import com.tuhuynh.httpserver.core.RequestUtils.RequestMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;

@RequiredArgsConstructor
public final class RequestBinderNIO {
    private final RequestContext requestContext;
    private final ArrayList<HandlerMetadata> handlerMetadata;
    private CompletableFuture<HttpResponse> isDone = new CompletableFuture<>();

    public CompletableFuture<HttpResponse> handlersProcess() throws Exception {
        var isFound = false;

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
                        requestContext.getParam().put(handlerParamKey, handlerParamValue);
                    }
                }
            }

            if ((requestContext.getMethod() == h.getMethod() || (h.getMethod() == RequestMethod.ALL))
                && (requestPath.equals(handlerPath) || requestWithHandlerParamsMatched)) {
                val handlerLinkedList = new LinkedList<>(Arrays.asList(h.handlers));
                doProcess(handlerLinkedList);
                isFound = true;
                break;
            }
        }

        if (!isFound) {
            isDone.complete(HttpResponse.of("Not found").status(404));
        }

        return isDone;
    }

    private void doProcess(final LinkedList<RequestHandler> handlerLinkedList) throws Exception {
        if (handlerLinkedList.size() == 1) {
            handlerLinkedList.removeFirst().handleFunc(requestContext).thenAccept(result -> {
                isDone.complete(result);
            });
        } else {
            handlerLinkedList.removeFirst().handleFunc(requestContext).thenAccept(result -> {
                if (result.isAllowNext()) {
                    try {
                        doProcess(handlerLinkedList);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    isDone.complete(result);
                }
            });
        }
    }

    @FunctionalInterface
    public interface RequestHandler {
        CompletableFuture<HttpResponse> handleFunc(RequestContext requestMetadata) throws Exception;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static final class HandlerMetadata {
        private RequestMethod method;
        private String path;
        private RequestHandler[] handlers;
    }
}
