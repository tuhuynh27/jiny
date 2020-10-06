package com.jinyframework.core.nio;

import com.jinyframework.core.RequestBinderBase;
import com.jinyframework.core.RequestBinderBase.HandlerNIO;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public final class RequestBinderNIO extends RequestBinderBase<HandlerNIO> {
    private final CompletableFuture<HttpResponse> isDone = new CompletableFuture<>();

    public RequestBinderNIO(RequestContext requestContext,
                            final List<HandlerMetadata<HandlerNIO>> middlewares,
                            final List<HandlerMetadata<HandlerNIO>> handlerMetadata) {
        super(requestContext, middlewares, handlerMetadata);
    }

    public CompletableFuture<HttpResponse> getResponseObject() {
        var isFound = false;

        for (val h : handlerMetadata) {
            val binder = binderInit(h);

            if (binder.isMatchCatchAll() ||
                    (requestContext.getMethod() == h.getMethod() || (h.getMethod() == HttpMethod.ALL))
                            && (binder.getRequestPath().equals(binder.getHandlerPath()) || binder
                            .isRequestWithHandlerParamsMatched())) {
                val middlewareMatched = middlewares.stream()
                        .filter(e -> requestContext.getPath().startsWith(e.getPath()))
                        .map(HandlerMetadata::getHandlers)
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

    private void resolvePromiseChain(final LinkedList<HandlerNIO> handlerQueue) {
        if (handlerQueue.size() == 1) {
            try {
                handlerQueue.removeFirst().handleFunc(requestContext).thenAccept(isDone::complete);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                isDone.complete(HttpResponse.of(e.getMessage()).status(500));
            }
        } else {
            try {
                handlerQueue.removeFirst().handleFunc(requestContext).thenAccept(result -> {
                    if (result.isAllowNext()) {
                        try {
                            resolvePromiseChain(handlerQueue);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                            isDone.complete(HttpResponse.of(e.getMessage()).status(500));
                        }
                    } else {
                        isDone.complete(result);
                    }
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                isDone.complete(HttpResponse.of(e.getMessage()).status(500));
            }
        }
    }
}
