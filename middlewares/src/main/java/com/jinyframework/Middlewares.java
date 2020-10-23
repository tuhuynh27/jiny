package com.jinyframework;

import com.jinyframework.core.AbstractRequestBinder.Context;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;
import lombok.val;

public final class Middlewares {
    public static String test() {
        // TODO: Sample code, need to be updated when done CORS middleware
        val context = Context.builder()
                .path("/").method(HttpMethod.GET).build();
        return context.getBody();
    }
}
