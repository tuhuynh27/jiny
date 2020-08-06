package com.tuhuynh.jerrymouse.core.nio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tuhuynh.jerrymouse.core.ParserUtils.RequestMethod;
import com.tuhuynh.jerrymouse.core.RequestBinder;

import lombok.val;
import lombok.var;

public final class RequestBinderNIO extends RequestBinder {
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

            if (binder.isMatchCatchAll() ||
                (requestContext.getMethod() == h.getMethod() || (h.getMethod() == RequestMethod.ALL))
                && (binder.getRequestPath().equals(binder.getHandlerPath()) || binder
                        .isRequestWithHandlerParamsMatched())) {
                val handlers = Arrays.asList(h.handlers);
                val handlerLinkedList = Stream.concat(middlewares.stream(), handlers.stream()).
                        collect(Collectors.toCollection(LinkedList::new));
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
            try {
                handlerQueue.removeFirst().handleFunc(requestContext).thenAccept(result -> {
                    isDone.complete(result);
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
                isDone.complete(HttpResponse.of(e.getMessage()).status(500));
            }
        } else {
            try {
                handlerQueue.removeFirst().handleFunc(requestContext).thenAccept(result -> {
                    if (result.isAllowNext()) {
                        try {
                            resolvePromiseChain(handlerQueue);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            isDone.complete(HttpResponse.of(e.getMessage()).status(500));
                        }
                    } else {
                        isDone.complete(result);
                    }
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
                isDone.complete(HttpResponse.of(e.getMessage()).status(500));
            }
        }
    }
}
