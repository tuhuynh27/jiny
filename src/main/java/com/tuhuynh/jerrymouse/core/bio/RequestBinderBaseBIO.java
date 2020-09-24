package com.tuhuynh.jerrymouse.core.bio;

import com.tuhuynh.jerrymouse.core.RequestBinderBase;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.RequestHandlerBIO;
import com.tuhuynh.jerrymouse.core.utils.ParserUtils.HttpMethod;
import lombok.val;
import lombok.var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RequestBinderBaseBIO extends RequestBinderBase<RequestHandlerBIO> {
    public RequestBinderBaseBIO(final RequestContext requestContext,
                                final ArrayList<BaseHandlerMetadata<RequestHandlerBIO>> middlewares,
                                final ArrayList<BaseHandlerMetadata<RequestHandlerBIO>> handlerMetadata) {
        super(requestContext, middlewares, handlerMetadata);
    }

    public HttpResponse getResponseObject() {
        for (var h : handlerMetadata) {
            val binder = binderInit(h);

            if (binder.isMatchCatchAll() ||
                    (requestContext.getMethod() == h.getMethod() || (h.getMethod() == HttpMethod.ALL))
                            && (binder.getRequestPath().equals(binder.getHandlerPath()) || binder
                            .isRequestWithHandlerParamsMatched())) {
                try {
                    // Handle middleware function chain
                    val middlewareMatched = middlewares.stream()
                            .filter(e -> requestContext.getPath().startsWith(e.getPath()))
                            .map(BaseHandlerMetadata::getHandlers)
                            .flatMap(e -> Arrays.stream(e).distinct())
                            .collect(Collectors.toList());
                    val handlers = Arrays.asList(h.handlers);

                    val handlersAndMiddlewares = Stream
                            .concat(middlewareMatched.stream(), handlers.stream())
                            .collect(Collectors.toList());

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
