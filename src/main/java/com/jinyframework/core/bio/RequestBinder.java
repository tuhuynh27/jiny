package com.jinyframework.core.bio;

import com.jinyframework.core.RequestBinderBase;
import com.jinyframework.core.RequestBinderBase.Handler;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public final class RequestBinder extends RequestBinderBase<Handler> {
    public RequestBinder(final Context context,
                         final List<HandlerMetadata<Handler>> middlewares,
                         final List<HandlerMetadata<Handler>> handlerMetadata) {
        super(context, middlewares, handlerMetadata);
    }

    public HttpResponse getResponseObject() throws Exception {
        val middlewaresMatched = middlewares.stream()
                .filter(e -> context.getPath().startsWith(e.getPath()))
                .map(HandlerMetadata::getHandlers)
                .flatMap(e -> Arrays.stream(e).distinct())
                .collect(Collectors.toList());

        for (var h : handlerMetadata) {
            val binder = binderInit(h);

            if (binder.isMatchCatchAll() ||
                    (context.getMethod() == h.getMethod() || (h.getMethod() == HttpMethod.ALL))
                            && (binder.getRequestPath().equals(binder.getHandlerPath()) || binder
                            .isRequestWithHandlerParamsMatched())) {
                try {
                    val handlersMatched = Arrays.asList(h.handlers);
                    val handlersAndMiddlewares = Stream
                            .concat(middlewaresMatched.stream(), handlersMatched.stream())
                            .collect(Collectors.toList());
                    return resolveHandlerChain(handlersAndMiddlewares, null);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return HttpResponse.of(e.getMessage()).status(500);
                }
            }
        }

        return resolveHandlerChain(middlewaresMatched, HttpResponse.of("Not found").status(404));
    }

    public HttpResponse resolveHandlerChain(@NonNull final List<RequestBinderBase.Handler> handlers, final HttpResponse customResponse) throws Exception {
        for (var i = 0; i < handlers.size(); i++) {
            val isLastItem = (i == handlers.size() - 1);
            val resultFromPreviousHandler = handlers.get(i).handleFunc(context);
            if (!isLastItem && !resultFromPreviousHandler.isAllowNext()) {
                return resultFromPreviousHandler;
            } else {
                if (isLastItem) {
                    if (customResponse != null) {
                        return customResponse;
                    }

                    return resultFromPreviousHandler;
                }
            }
        }

        log.error("Cannot resolve handler chain");
        return HttpResponse.of("Cannot resolve handler chain").status(500);
    }
}
