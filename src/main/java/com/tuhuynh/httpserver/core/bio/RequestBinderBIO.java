package com.tuhuynh.httpserver.core.bio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

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
            val indexOfQuestionMark = requestContext.getPath().indexOf('?');
            var requestPath =
                    indexOfQuestionMark == -1 ? requestContext.getPath() : requestContext.getPath().substring(0,
                                                                                                              indexOfQuestionMark);
            // Remove all last '/' from the requestPath
            while (requestPath.endsWith("/")) {
                requestPath = requestPath.substring(0, requestPath.length() - 1);
            }

            val handlerPathOriginal = h.getPath();
            val handlerPathArrWithHandlerParams = Arrays.stream(handlerPathOriginal.split("/"));
            val handlerPath = handlerPathArrWithHandlerParams.filter(e -> !e.startsWith(":")).collect(
                    Collectors.joining("/"));

            val numOfHandlerParams = handlerPathOriginal.length() - handlerPathOriginal.replace(":", "")
                                                                                       .length();
            val numOfSlashOfRequestPath = requestPath.length() - requestPath.replace("/", "").length();
            val numOfSlashOfHandlerPath = handlerPathOriginal.length() - handlerPathOriginal.replace("/", "")
                                                                                            .length();

            val requestWithHandlerParamsMatched = numOfHandlerParams > 0 && requestPath.startsWith(handlerPath)
                                                  && numOfSlashOfRequestPath == numOfSlashOfHandlerPath;

            if (requestWithHandlerParamsMatched) {
                val elementsOfHandlerPath = handlerPathOriginal.split("/");
                val elementsOfRequestPath = requestPath.split("/");
                for (int i = 1; i < elementsOfHandlerPath.length; i++) {
                    if (elementsOfHandlerPath[i].startsWith(":")) {
                        val handlerParamKey = elementsOfHandlerPath[i].replace(":", "");
                        val handlerParamValue = elementsOfRequestPath[i];
                        requestContext.getParam().put(handlerParamKey, handlerParamValue);
                    }
                }
            }

            if ((requestContext.getMethod() == h.getMethod() || (h.getMethod() == RequestMethod.ALL))
                && (requestPath.equals(handlerPath) || requestWithHandlerParamsMatched)) {
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
