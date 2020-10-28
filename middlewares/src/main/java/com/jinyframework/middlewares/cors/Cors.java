package com.jinyframework.middlewares.cors;

import com.jinyframework.core.AbstractRequestBinder.Context;
import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;
import lombok.*;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Cors {
    public static Config allowDefault() {
        return Config.defaultBuilder().build();
    }

    public static Config allowAll() {
        return Config.builder()
                .allowAllOrigins(true)
                .allowCredentials(false)
                .optionPass(false)
                .build();
    }

    public static Handler newHandler(@NonNull Config config) {
        return ctx -> {
            if (ctx.getMethod() == HttpMethod.OPTIONS
                    && !ctx.headerParam("Access-Control-Request-Method").isEmpty()) {
                handlePreflight(ctx, config);
                if (config.optionPass) {
                    return HttpResponse.next();
                } else {
                    return HttpResponse.of("", HttpURLConnection.HTTP_NO_CONTENT);
                }
            } else {
                handleActual(ctx, config);
                return HttpResponse.next();
            }
        };
    }

    private static void handlePreflight(Context ctx, @NonNull Config config) {
        val origin = ctx.headerParam("Origin");

        ctx.putHeader("Vary",
                String.join(",", "Origin", "Access-Control-Request-Method",
                        "Access-Control-Request-Headers"));

        if (origin.isEmpty()) {
            return;
        }

        val reqMethod = ctx.headerParam("Access-Control-Request-Method");
        if (ctx.getMethod() != HttpMethod.OPTIONS
                && !config.allowMethods.isEmpty()
                && config.allowMethods.stream().noneMatch(reqMethod::equalsIgnoreCase)) {
            return;
        }

        val reqHeaders = new ArrayList<>(Arrays.asList(ctx.headerParam("Access-Control-Request-Headers")
                .split(",")));
        if (!isAllowedHeaders(reqHeaders, config)) {
            return;
        }

        ctx.putHeader("Access-Control-Allow-Methods", reqMethod);

        if (!reqHeaders.get(0).isEmpty()) {
            if (!reqHeaders.contains("Origin")) {
                reqHeaders.add("Origin");
            }
            ctx.putHeader("Access-Control-Allow-Headers", reqHeaders.stream()
                    .map(Util::normalizeHeader)
                    .collect(Collectors.joining(",")));
        }

        if (config.allowAllOrigins || config.allowOrigins.contains("*")) {
            ctx.putHeader("Access-Control-Allow-Origin", "*");
        } else if (isAllowedOrigin(origin, config)) {
            ctx.putHeader("Access-Control-Allow-Origin", origin);
        }

        if (config.allowCredentials) {
            ctx.putHeader("Access-Control-Allow-Credentials", "true");
        }

        if (config.maxAge > 0) {
            ctx.putHeader("Access-Control-Max-Age", String.valueOf(config.maxAge));
        }
    }

    private static void handleActual(Context ctx, Config config) {
        val origin = ctx.headerParam("Origin");

        ctx.putHeader("Vary", "Origin");

        if (origin.isEmpty()) {
            return;
        }

        if (config.allowAllOrigins || config.allowOrigins.contains("*")) {
            ctx.putHeader("Access-Control-Allow-Origin", "*");
        } else if (config.allowOrigins.contains(origin)) {
            ctx.putHeader("Access-Control-Allow-Origin", origin);
        }

        if (config.allowCredentials) {
            ctx.putHeader("Access-Control-Allow-Credentials", "true");
        }

        if (!config.exposeHeaders.isEmpty()) {
            ctx.putHeader("Access-Control-Expose-Headers", String.join(",", config.exposeHeaders));
        }
    }

    private static boolean isAllowedOrigin(String origin, Config config) {
        return config.allowOrigins.stream().anyMatch(org -> org.equalsIgnoreCase(origin));
    }

    private static boolean isAllowedHeaders(List<String> reqHeaders, Config config) {
        if (reqHeaders.get(0).isEmpty()
                || config.allowHeaders.isEmpty()
                || config.allowHeaders.contains("*")) {
            return true;
        }
        return reqHeaders.stream()
                .allMatch(header -> config.allowHeaders
                        .stream()
                        .anyMatch(header::equalsIgnoreCase));
    }

    public static Handler newHandler() {
        return newHandler(allowDefault());
    }

    @Getter
    @Builder
    public static final class Config {
        private final boolean allowAllOrigins;
        private final boolean allowCredentials;
        @Singular
        private final List<String> exposeHeaders;
        @Singular
        private final List<String> allowOrigins;
        @Singular
        private final List<String> allowMethods;
        @Singular
        private final List<String> allowHeaders;
        private final boolean optionPass;
        private final int maxAge;

        public static ConfigBuilder defaultBuilder() {
            val allowMethods = Stream.of("GET", "POST", "HEAD").collect(Collectors.toList());
            val allowHeaders = Stream.of("Origin", "Accept", "Content-Type", "X-Requested-With")
                    .collect(Collectors.toList());
            return builder()
                    .allowAllOrigins(true)
                    .allowCredentials(false)
                    .allowMethods(allowMethods)
                    .allowHeaders(allowHeaders)
                    .optionPass(false)
                    .maxAge(0);
        }
    }
}
