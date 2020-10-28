package com.jinyframework.core.nio;

import com.jinyframework.core.AbstractRequestBinder;
import com.jinyframework.core.AbstractRequestBinder.HandlerNIO;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;
import lombok.NonNull;
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
public final class RequestBinderNIO extends AbstractRequestBinder<HandlerNIO> {
    private final CompletableFuture<HttpResponse> promise = new CompletableFuture<>();

    public RequestBinderNIO(final Context context,
                            final List<HandlerMetadata<HandlerNIO>> middlewares,
                            final List<HandlerMetadata<HandlerNIO>> handlerMetadata) {
        super(context, middlewares, handlerMetadata);
    }

    public CompletableFuture<HttpResponse> getResponseObject() {
        var is404 = true;
        val middlewaresMatched = middlewares.stream()
                .filter(e -> context.getPath().startsWith(e.getPath()))
                .map(HandlerMetadata::getHandlers)
                .flatMap(e -> Arrays.stream(e).distinct())
                .collect(Collectors.toList());

        for (val h : handlerMetadata) {
            val binder = binderInit(h);

            if (binder.isMatchCatchAll() && (context.getMethod() == h.getMethod() || h.getMethod() == HttpMethod.ALL) ||
                    (context.getMethod() == h.getMethod() || (h.getMethod() == HttpMethod.ALL))
                            && (binder.getRequestPath().equals(binder.getHandlerPath()) || binder
                            .isRequestWithHandlerParamsMatched())) {
                val handlersMatched = Arrays.asList(h.handlers);
                val handlerLinkedList = Stream.concat(middlewaresMatched.stream(), handlersMatched.stream()).
                        collect(Collectors.toCollection(LinkedList::new));
                resolvePromiseChain(handlerLinkedList, null);
                is404 = false;
                break;
            }
        }

        if (is404) {
            resolvePromiseChain(new LinkedList<>(middlewaresMatched), HttpResponse.of("Not found").status(404));
        }

        return promise;
    }

    private void resolvePromiseChain(@NonNull final LinkedList<HandlerNIO> handlerQueue, final HttpResponse customResult) {
        if (handlerQueue.size() == 0) {
            if (customResult != null) {
                promise.complete(customResult);
            } else {
                log.error("Internal Error: Handler chain is empty");
                promise.complete(HttpResponse.of("Handler chain is empty").status(500));
            }
        } else if (handlerQueue.size() == 1) {
            try {
                handlerQueue.removeFirst().handleFunc(context).thenAccept(result -> {
                    if (customResult != null) {
                        promise.complete(customResult);
                    } else {
                        promise.complete(result);
                    }
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                promise.complete(HttpResponse.of(e.getMessage()).status(500));
            }
        } else {
            try {
                handlerQueue.removeFirst().handleFunc(context).thenAccept(result -> {
                    if (result.isAllowNext()) {
                        try {
                            resolvePromiseChain(handlerQueue, customResult);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                            promise.complete(HttpResponse.of(e.getMessage()).status(500));
                        }
                    } else {
                        if (customResult != null) {
                            promise.complete(customResult);
                        } else {
                            promise.complete(result);
                        }
                    }
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                promise.complete(HttpResponse.of(e.getMessage()).status(500));
            }
        }
    }
}
