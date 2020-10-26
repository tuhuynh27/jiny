package com.jinyframework.middlewares.cors;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jinyframework.core.AbstractRequestBinder;
import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.core.utils.ParserUtils;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.val;

public final class Cors {
    private Cors() {
    }

    public static Config defaultConfig() {
        final List<String> allowMethods = Stream.of("GET", "POST", "HEAD").collect(Collectors.toList());
        final List<String> allowHeaders = Stream.of("Origin", "Accept", "Content-Type", "X-Requested-With")
                                                .collect(
                                                        Collectors.toList());
        return Config.builder()
                     .allowAll(true)
                     .allowCredentials(false)
                     .allowMethods(allowMethods)
                     .allowHeaders(allowHeaders)
                     .optionPass(false)
                     .build();
    }

    public static Handler newHandler(Config config) {
        return ctx -> {
            if (ctx.getMethod() == ParserUtils.HttpMethod.OPTIONS
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

    private static void handlePreflight(AbstractRequestBinder.Context ctx, Config config) {
        System.out.println("PREFLIGHT");
        val origin = ctx.headerParam("Origin");

        ctx.putHeader("Vary",
                      String.join(",", "Origin", "Access-Control-Request-Method",
                                  "Access-Control-Request-Headers"));

        if (origin.isEmpty()) {
            return;
        }
        val reqMethod = ctx.headerParam("Access-Control-Request-Method");
        if (ctx.getMethod() != HttpMethod.OPTIONS
            && config.allowMethods.stream().noneMatch(reqMethod::equalsIgnoreCase)) {
            return;
        }
        val reqHeaders = new ArrayList<>(Arrays.asList(ctx.headerParam("Access-Control-Request-Headers")
                                          .split(",")));
        if (!isAllowedHeaders(reqHeaders, config)) {
            return;
        }

        if (config.allowAll) {
            ctx.putHeader("Access-Control-Allow-Origin", "*");
        } else if (config.allowOrigins.contains(origin)) {
            ctx.putHeader("Access-Control-Allow-Origin", origin);
        }

        ctx.putHeader("Access-Control-Allow-Methods",
                      config.allowMethods.stream()
                                         .map(String::toUpperCase)
                                         .collect(Collectors.joining(",")));
        if (!reqHeaders.get(0).isEmpty()) {
            if (!reqHeaders.contains("Origin")) {
                reqHeaders.add("Origin");
            }
            ctx.putHeader("Access-Control-Allow-Headers",
                          reqHeaders.stream()
                                    .map(Util::normalizeHeader)
                                    .collect(Collectors.joining(",")));
        }
        if (config.allowAll) {
            ctx.putHeader("Access-Control-Allow-Origin", "*");
        } else if (config.allowOrigins.contains(origin)) {
            ctx.putHeader("Access-Control-Allow-Origin", origin);
        }

        if (config.allowCredentials) {
            ctx.putHeader("Access-Control-Allow-Credentials", "true");
        }

        if (config.maxAge > 0) {
            ctx.putHeader("Access-Control-Max-Age", String.valueOf(config.maxAge));
        }
    }

    private static void handleActual(AbstractRequestBinder.Context ctx, Config config) {
        val origin = ctx.headerParam("Origin");

        ctx.putHeader("Vary", "Origin");

        if (origin.isEmpty()) {
            return;
        }
        if (config.allowAll) {
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

    private static boolean isAllowedHeaders(List<String> reqHeaders, Config config) {
        if (reqHeaders.get(0).isEmpty()) {
            return true;
        }
        final boolean isAllowedHeaders = reqHeaders.stream()
                                                   .allMatch(header -> config.allowHeaders
                                                           .stream()
                                                           .anyMatch(header::equalsIgnoreCase));
        System.out.println(reqHeaders.toString() +
                           config.allowHeaders + reqHeaders.size() + isAllowedHeaders);
        return isAllowedHeaders;
    }

    public static Handler newHandler() {
        return newHandler(defaultConfig());
    }

    @Getter
    @Builder
    public static final class Config {
        private final boolean allowAll;
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
    }
}
