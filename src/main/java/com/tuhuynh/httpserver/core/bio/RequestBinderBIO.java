package com.tuhuynh.httpserver.core.bio;

import java.io.IOException;
import java.util.ArrayList;

import com.tuhuynh.httpserver.core.RequestBinderBase;
import com.tuhuynh.httpserver.core.RequestUtils.RequestMethod;

import lombok.val;
import lombok.var;

public final class RequestBinderBIO extends RequestBinderBase {
    private final ArrayList<BaseHandlerMetadata<RequestHandlerBIO>> handlerMetadata;

    public RequestBinderBIO(RequestContext requestContext,
                            ArrayList<BaseHandlerMetadata<RequestHandlerBIO>> handlerMetadata) {
        super(requestContext);
        this.handlerMetadata = handlerMetadata;
    }

    public HttpResponse getResponseObject() throws IOException {
        for (var h : handlerMetadata) {
            val binder = binderInit(h);

            if ((requestContext.getMethod() == h.getMethod() || (h.getMethod() == RequestMethod.ALL))
                && (binder.getRequestPath().equals(binder.getHandlerPath()) || binder
                    .isRequestWithHandlerParamsMatched())) {
                try {
                    // Handle middleware function chain
                    for (int i = 0; i < h.handlers.length; i++) {
                        val isLastItem = i == h.handlers.length - 1;
                        val resultFromPreviousHandler = h.handlers[i].handleFunc(requestContext);
                        if (!isLastItem && !resultFromPreviousHandler.isAllowNext()) {
                            return resultFromPreviousHandler;
                        } else {
                            if (isLastItem) {
                                return resultFromPreviousHandler;
                            } else {
                                continue;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return HttpResponse.of("Internal Server Error").status(500);
                }
            }
        }

        return HttpResponse.of("Not found").status(404);
    }
}
