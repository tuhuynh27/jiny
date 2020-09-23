package com.tuhuynh.jerrymouse.core.nio;

import com.tuhuynh.jerrymouse.core.ParserUtils.RequestMethod;
import com.tuhuynh.jerrymouse.core.RequestBinder;
import lombok.val;
import lombok.var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RequestBinderNIO extends RequestBinder {
    private final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> middlewares;
    private final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> handlerMetadata;
    private final CompletableFuture<HttpResponse> isDone = new CompletableFuture<>();

    public RequestBinderNIO(RequestContext requestContext,
                            final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> middlewares,
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
                val middlewareMatched = middlewares.stream()
                        .filter(e -> requestContext.getPath().startsWith(e.getPath()))
                        .map(BaseHandlerMetadata::getHandlers)
                        .flatMap(e -> Arrays.stream(e).distinct())
                        .collect(Collectors.toList());
                val handlers = Arrays.asList(h.handlers);
                val handlerLinkedList = Stream.concat(middlewareMatched.stream(), handlers.stream()).
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
                handlerQueue.removeFirst().handleFunc(requestContext).thenAccept(isDone::complete);
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
