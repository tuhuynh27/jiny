package com.tuhuynh.jerrymouse.core.bio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tuhuynh.jerrymouse.core.ParserUtils.RequestMethod;
import com.tuhuynh.jerrymouse.core.RequestBinder;

import lombok.val;
import lombok.var;

public final class RequestBinderBIO extends RequestBinder {
    private final ArrayList<RequestHandlerBIO> middlewares;
    private final ArrayList<BaseHandlerMetadata<RequestHandlerBIO>> handlerMetadata;

    public RequestBinderBIO(RequestContext requestContext,
                            final ArrayList<RequestHandlerBIO> middlewares,
                            final ArrayList<BaseHandlerMetadata<RequestHandlerBIO>> handlerMetadata) {
        super(requestContext);
        this.middlewares = middlewares;
        this.handlerMetadata = handlerMetadata;
    }

    public HttpResponse getResponseObject() throws IOException {
        for (var h : handlerMetadata) {
            val binder = binderInit(h);

            if (binder.isMatchCatchAll() ||
                (requestContext.getMethod() == h.getMethod() || (h.getMethod() == RequestMethod.ALL))
                && (binder.getRequestPath().equals(binder.getHandlerPath()) || binder
                        .isRequestWithHandlerParamsMatched())) {
                try {
                    // Handle middleware function chain
                    val handlers = Arrays.asList(h.handlers);
                    val handlersAndMiddlewares = Stream.concat(middlewares.stream(), handlers.stream()).collect(
                            Collectors.toList());
                    val size = handlersAndMiddlewares.size();
                    for (int i = 0; i < size; i++) {
                        val isLastItem = (i == size - 1);
                        val resultFromPreviousHandler = handlersAndMiddlewares.get(i).handleFunc(
                                requestContext);
                        if (!isLastItem && !resultFromPreviousHandler.isAllowNext()) {
                            return resultFromPreviousHandler;
                        } else {
                            if (isLastItem) {
                                return resultFromPreviousHandler;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return HttpResponse.of(e.getMessage()).status(500);
                }
            }
        }

        return HttpResponse.of("Not found").status(404);
    }
}
