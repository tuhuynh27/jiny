package com.jinyframework.middlewares;

import java.util.List;
import java.util.stream.Collectors;

import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.val;

public final class Cors {
    private Cors() {}

    @Getter
    @Builder
    public static final class Config {
        private final boolean allowAll;
        private final boolean allowCredentials;
        @Singular
        private final List<String> exposeHeaders;
        @Singular
        private final List<String> allowOrigins;
    }

    public static Config defaultConfig() {
        return Config.builder()
                     .allowAll(true)
                     .allowCredentials(false)
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

            if (config.allowCredentials) {
                ctx.putHeader("Access-Control-Allow-Credentials","true");
            }

            if (!config.exposeHeaders.isEmpty()) {
                ctx.putHeader("Access-Control-Expose-Headers", String.join(",", config.exposeHeaders));
            }
            return HttpResponse.next();
        };
    }
    public static Handler newHandler() {
        return newHandler(defaultConfig());
    }
}
