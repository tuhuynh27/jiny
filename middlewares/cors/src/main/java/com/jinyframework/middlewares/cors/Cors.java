package com.jinyframework.middlewares.cors;

import com.jinyframework.core.AbstractRequestBinder.Context;
import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;
import lombok.*;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jinyframework.middlewares.cors.Cors.Config.*;

/**
 * Middleware to help handle Cross-Origin Resource Sharing
 * <p>
 * Specification: https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
 */
public final class Cors {
    private static final String VARY_HEADERS = String.join(",", "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers");

    private Cors() {
    }

    /**
     * A configuration that allows "simple requests" for all origins.
     *
     * @return the config
     */
    public static Config allowDefault() {
        return withDefault();
    }

    /**
     * A configuration that allows all origins and headers.
     *
     * @return the config
     */
    public static Config allowAll() {
        return builder()
                .allowAllOrigins(true)
                .allowAllHeaders(true)
                .allowCredentials(false)
                .optionPass(false)
                .build();
    }

    /**
     * Creates new CORS handler with provided configuration.
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
            builder.allowHeaders(ALLOW_HEADERS_DEFAULT);
        } else if (config.allowHeaders.contains("*")) {
            builder.allowAllHeaders(true);
            builder.clearAllowHeaders();
        } else {
            builder.allowHeader("Origin");
        }

        if (config.allowMethods.isEmpty()) {
            builder.allowMethods(ALLOW_METHODS_DEFAULT);
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

    private static void handlePreflight(Context ctx, @NonNull Config config) {
        val origin = ctx.headerParam("Origin");

        ctx.putHeader("Vary", VARY_HEADERS);

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
        for (val am : allowMethods) {
            if (am.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAllowedOrigin(String origin, List<String> allowOrigins) {
        for (val org : allowOrigins) {
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
        for (val header : reqHeaders) {
            var isAllow = false;
            for (val allowHeader : allowHeaders) {
                if (allowHeader.equalsIgnoreCase(header)) {
                    isAllow = true;
                    break;
                }
            }
            if (!isAllow) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create new CORS handler with default settings.
     *
     * @return the handler
     */
    public static Handler newHandler() {
        return newHandler(allowDefault());
    }

    /**
     * Used to configure CORS options
     */
    @Getter
    @Builder(toBuilder = true)
    public static final class Config {
        public static final List<String> ALLOW_HEADERS_DEFAULT = Stream.of("Origin", "Accept", "Content-Type",
                "X-Requested-With")
                .collect(Collectors.toList());
        public static final List<String> ALLOW_METHODS_DEFAULT = Stream.of("GET", "POST", "HEAD").collect(
                Collectors.toList());
        /**
         * Accept request from any origins.
         * Defaults to {@code false} but will be set to {@code true}
         * if {@link #allowOrigins} is empty or contains {@code "*"}.
         */
        private final boolean allowAllOrigins;
        /**
         * Accept request with any headers.
         * Defaults to {@code false} but will be set to {@code true}
         * if {@link #allowHeaders} contains {@code "*"}.
         */
        private final boolean allowAllHeaders;
        /**
         * If true, the credential header will be set in response to make Cookies available.
         */
        private final boolean allowCredentials;
        /**
         * Whitelisted headers that are made available for client to access.
         */
        @Singular
        private final List<String> exposeHeaders;
        /**
         * Whitelisted origins that the server will accept.
         * If list is empty, all origins are permitted.
         */
        @Singular
        private final List<String> allowOrigins;
        /**
         * Whitelisted methods that the server will accept.
         * If list is empty, it will be set to
         * {@code GET}, {@code POST}, {@code HEAD} (see {@link #ALLOW_METHODS_DEFAULT})
         */
        @Singular
        private final List<String> allowMethods;
        /**
         * Whitelisted headers that the server will accept in a request.
         * {@code Origin} header will automatically be added as well.
         * If list is empty, it will be set to
         * {@code Origin}, {@code Accept}, {@code Content-Type}, {@code X-Requested-With}
         * (see {@link #ALLOW_HEADERS_DEFAULT})
         */
        @Singular
        private final List<String> allowHeaders;
        /**
         * Continue processing for OPTION preflight request. Defaults to {@code false}
         */
        private final boolean optionPass;
        /**
         * The maximum age (in seconds) of the cache duration for preflight responses.
         * Defaults is {@code 0} (header not set)
         */
        private final int maxAge;

        /**
         * Default ConfigBuilder that allows "simple request" for all origins.
         *
         * @return the config builder
         */
        public static Config withDefault() {
            return builder()
                    .allowAllOrigins(true)
                    .allowCredentials(false)
                    .allowMethods(ALLOW_METHODS_DEFAULT)
                    .allowHeaders(ALLOW_HEADERS_DEFAULT)
                    .optionPass(false)
                    .maxAge(0)
                    .build();
        }
    }
}
