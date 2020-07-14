package com.tuhuynh.httpserver.core.nio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

import com.tuhuynh.httpserver.core.RequestBinderBase;
import com.tuhuynh.httpserver.core.RequestUtils.RequestMethod;

import lombok.val;
import lombok.var;

public final class RequestBinderNIO extends RequestBinderBase {
    private final ArrayList<RequestHandlerNIO> middlewares;
    private final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> handlerMetadata;
    private CompletableFuture<HttpResponse> isDone = new CompletableFuture<>();

    public RequestBinderNIO(RequestContext requestContext,
                            final ArrayList<RequestHandlerNIO> middlewares,
                            final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> handlerMetadata) {
        super(requestContext);
        this.middlewares = middlewares;
        this.handlerMetadata = handlerMetadata;
    }

    public CompletableFuture<HttpResponse> getResponseObject() throws Exception {
        var isFound = false;

        for (val h : handlerMetadata) {
            val binder = binderInit(h);

            if ((requestContext.getMethod() == h.getMethod() || (h.getMethod() == RequestMethod.ALL))
                && (binder.getRequestPath().equals(binder.getHandlerPath()) || binder
                    .isRequestWithHandlerParamsMatched())) {
                middlewares.addAll(Arrays.asList(h.handlers));
                val handlerLinkedList = new LinkedList<>(middlewares);
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
