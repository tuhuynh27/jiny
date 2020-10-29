package com.jinyframework.middlewares.cors;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jinyframework.core.AbstractRequestBinder.Context;
import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.val;

/**
 * The type Cors.
 */
public final class Cors {
    /**
     * Allow default config.
     *
     * @return the config
     */
    public static Config allowDefault() {
        return Config.defaultBuilder().build();
    }

    /**
     * Allow all config.
     *
     * @return the config
     */
    public static Config allowAll() {
        return Config.builder()
                     .allowAllOrigins(true)
                     .allowAllHeaders(true)
                     .allowCredentials(false)
                     .optionPass(false)
                     .build();
    }

    /**
     * New handler handler.
     *
     * @param config the config
     * @return the handler
     */
    public static Handler newHandler(@NonNull Config config) {
        // apply sensible defaults in case users haven't set them
        val builder = config.toBuilder();

        if (config.allowOrigins.isEmpty() || config.allowOrigins.contains("*")) {
            builder.allowAllOrigins(true);
            builder.clearAllowOrigins();
        }

        if (config.allowHeaders.isEmpty()) {
            builder.allowHeaders(Config.allowHeadersDefault);
        } else if (config.allowHeaders.contains("*")) {
            builder.allowAllHeaders(true);
            builder.clearAllowHeaders();
        } else {
            builder.allowHeader("Origin");
        }

        if (config.allowMethods.isEmpty()) {
            builder.allowMethods(Config.allowMethodsDefault);
        }

        val finalConfig = builder.build();
        return ctx -> {
            if (ctx.getMethod() == HttpMethod.OPTIONS
                && !ctx.headerParam("Access-Control-Request-Method").isEmpty()) {
                handlePreflight(ctx, finalConfig);
                if (finalConfig.optionPass) {
                    return HttpResponse.next();
                } else {
                    return HttpResponse.of("", HttpURLConnection.HTTP_NO_CONTENT);
                }
            } else {
                handleActual(ctx, finalConfig);
                return HttpResponse.next();
            }
        };
    }

    private static final String varyHeaders = String.join(",", "Origin",
                                                          "Access-Control-Request-Method",
                                                          "Access-Control-Request-Headers");

    private static void handlePreflight(Context ctx, @NonNull Config config) {
        val origin = ctx.headerParam("Origin");

        ctx.putHeader("Vary", varyHeaders);

        if (origin.isEmpty()) {
            return;
        }

        val reqMethod = ctx.headerParam("Access-Control-Request-Method");
        if (ctx.getMethod() != HttpMethod.OPTIONS
            || !isAllowedMethod(reqMethod, config.getAllowMethods())) {
            return;
        }

        val reqHeaders = ctx.headerParam("Access-Control-Request-Headers")
                            .split(",");
        if (!config.allowAllHeaders && !isAllowedHeaders(reqHeaders, config.allowHeaders)) {
            return;
        }

        ctx.putHeader("Access-Control-Allow-Methods", reqMethod);

        if (!reqHeaders[0].isEmpty()) {
            ctx.putHeader("Access-Control-Allow-Headers", Stream.concat(Arrays.stream(reqHeaders),
                                                                        Stream.of("Origin"))
                                                                .distinct()
                                                                .map(Util::normalizeHeader)
                                                                .collect(Collectors.joining(",")));
        } else {
            ctx.putHeader("Access-Control-Allow-Headers", String.join(",", config.allowHeaders));
        }

        if (config.allowAllOrigins) {
            ctx.putHeader("Access-Control-Allow-Origin", "*");
        } else if (isAllowedOrigin(origin, config.allowOrigins)) {
            ctx.putHeader("Access-Control-Allow-Origin", origin);
        }

        if (config.allowCredentials) {
            ctx.putHeader("Access-Control-Allow-Credentials", "true");
        }

        if (config.maxAge > 0) {
            ctx.putHeader("Access-Control-Max-Age", String.valueOf(config.maxAge));
        }
    }

    private static void handleActual(Context ctx, @NonNull Config config) {
        val origin = ctx.headerParam("Origin");

        ctx.putHeader("Vary", "Origin");

        if (origin.isEmpty()) {
            return;
        }

        if (config.allowAllOrigins) {
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

    private static boolean isAllowedMethod(String method, List<String> allowMethods) {
        for (String am : allowMethods) {
            if (am.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAllowedOrigin(String origin, List<String> allowOrigins) {
        for (String org : allowOrigins) {
            if (org.equalsIgnoreCase(origin)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAllowedHeaders(String[] reqHeaders, List<String> allowHeaders) {
        if (reqHeaders[0].isEmpty()) {
            return true;
        }
        for (String header : reqHeaders) {
            boolean allow = false;
            for (String allowHeader : allowHeaders) {
                if (allowHeader.equalsIgnoreCase(header)) {
                    allow = true;
                    break;
                }
            }
            if (!allow) {
                return false;
            }
        }
        return true;
    }

    /**
     * New handler handler.
     *
     * @return the handler
     */
    public static Handler newHandler() {
        return newHandler(allowDefault());
    }

    /**
     * The type Config.
     */
    @Getter
    @Builder(toBuilder = true)
    public static final class Config {
        private final boolean allowAllOrigins;
        private final boolean allowAllHeaders;
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

        static List<String> allowHeadersDefault = Stream.of("Origin", "Accept", "Content-Type",
                                                            "X-Requested-With")
                                                        .collect(Collectors.toList());
        static List<String> allowMethodsDefault = Stream.of("GET", "POST", "HEAD").collect(
                Collectors.toList());

        /**
         * Default builder config builder.
         *
         * @return the config builder
         */
        public static ConfigBuilder defaultBuilder() {
            return builder()
                    .allowAllOrigins(true)
                    .allowCredentials(false)
                    .allowMethods(allowMethodsDefault)
                    .allowHeaders(allowHeadersDefault)
                    .optionPass(false)
                    .maxAge(0);
        }
    }
}
