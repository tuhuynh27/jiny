package com.tuhuynh.httpserver.core.nio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.tuhuynh.httpserver.core.RequestBinderBase;
import com.tuhuynh.httpserver.core.RequestUtils.RequestMethod;

import lombok.val;
import lombok.var;

public final class RequestBinderNIO extends RequestBinderBase {
    private final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> handlerMetadata;
    private CompletableFuture<HttpResponse> isDone = new CompletableFuture<>();

    public RequestBinderNIO(RequestContext requestContext,
                            ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> handlerMetadata) {
        super(requestContext);
        this.handlerMetadata = handlerMetadata;
    }

    public CompletableFuture<HttpResponse> getResponseObject() throws Exception {
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
                resolvePromiseChain(handlerLinkedList);
                isFound = true;
                break;
            }
        }

        if (!isFound) {
            isDone.complete(HttpResponse.of("Not found").status(404));
        }

        return isDone;
    }

    private void resolvePromiseChain(final LinkedList<RequestHandlerNIO> handlerQueue) throws Exception {
        if (handlerQueue.size() == 1) {
            handlerQueue.removeFirst().handleFunc(requestContext).thenAccept(result -> {
                isDone.complete(result);
            });
        } else {
            handlerQueue.removeFirst().handleFunc(requestContext).thenAccept(result -> {
                if (result.isAllowNext()) {
                    try {
                        resolvePromiseChain(handlerQueue);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    isDone.complete(result);
                }
            });
        }
    }
}
