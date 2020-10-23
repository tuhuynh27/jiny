package com.jinyframework.middlewares;

import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.val;

import java.util.List;

public final class Cors {
    private Cors() {
    }

    public static Config defaultConfig() {
        return Config.builder()
                .allowAll(true)
                .build();
    }

    public static Handler newHandler(Config config) {
        return ctx -> {
            val origin = ctx.headerParam("Origin");
            ctx.putHeader("Vary", "Origin");
            if (origin.isEmpty()) {
                return HttpResponse.next();
            }
            if (config.allowAll) {
                ctx.putHeader("Access-Control-Allow-Origin", "*");
            } else if (config.allowOrigins.contains(origin)) {
                ctx.putHeader("Access-Control-Allow-Origin", origin);
            }
            return HttpResponse.next();
        };
    }

    public static Handler newHandler() {
        return newHandler(defaultConfig());
    }

    @Getter
    @Builder
    public static final class Config {
        private final boolean allowAll;
        @Singular
        private final List<String> allowOrigins;
    }
}
