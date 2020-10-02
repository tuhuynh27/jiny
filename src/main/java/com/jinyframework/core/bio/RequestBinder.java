package com.jinyframework.core.bio;

import com.jinyframework.core.RequestBinderBase;
import com.jinyframework.core.RequestBinderBase.Handler;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public final class RequestBinder extends RequestBinderBase<Handler> {
    public RequestBinder(final RequestContext requestContext,
                         final List<HandlerMetadata<Handler>> middlewares,
                         final List<HandlerMetadata<Handler>> handlerMetadata) {
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
                            .map(HandlerMetadata::getHandlers)
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
                    log.error(e.getMessage(), e);
                    return HttpResponse.of(e.getMessage()).status(500);
                }
            }
        }

        return HttpResponse.of("Not found").status(404);
    }
}
