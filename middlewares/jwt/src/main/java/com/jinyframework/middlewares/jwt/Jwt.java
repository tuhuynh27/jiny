package com.jinyframework.middlewares.jwt;

import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.*;
import lombok.experimental.Accessors;

import javax.crypto.SecretKey;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.jinyframework.core.AbstractRequestBinder.Context;

/**
 * Middleware to help with basic Json Web Token authentication
 */
public final class Jwt {

    /**
     * The constant AUTH_HEADER_KEY.
     */
    public static final String AUTH_HEADER_KEY = "Authorization";
    /**
     * The constant AUTH_HEADER_PREFIX.
     */
    public static final String AUTH_HEADER_PREFIX = "Bearer ";

    private Jwt() {
    }

    /**
     * Generate a Base64 encoded key string using provided algorithm. You should store this somewhere safe for reuse.
     *
     * @param algo name of signature algorithm
     * @return base64 encoded string
     */
    public static String genKey(String algo) {
        return Encoders.BASE64.encode(Keys.secretKeyFor(SignatureAlgorithm.forName(algo)).getEncoded());
    }

    /**
     * Create a wrapper class to store auth related handlers
     *
     * @param config the config
     * @return the auth component
     */
    @SneakyThrows
    public static AuthComponent newAuthComponent(@NonNull Config config) {
        if (config.secretKey == null) {
            throw new Exception("Secret key not provided");
        }

        val builder = config.toBuilder();

        if (config.algorithm == null) {
            builder.algorithm("HS256");
        }

        builder.secKeyObj(Keys.hmacShaKeyFor(config.secretKey.getBytes(StandardCharsets.UTF_8)));

        if (config.userKey == null) {
            builder.userKey(Config.USER_KEY_DEFAULT);
        }
        if (config.userRetriever == null) {
            builder.userRetriever((ctx, claims) -> String.valueOf(claims.get(Claims.SUBJECT)));
        }

        if (config.authenticator == null) {
            builder.authenticator(ctx -> new HashMap<>());
        }

        if (config.okHandler == null) {
            builder.okHandler((ctx, token, claims) -> {
                val authHeaderVal = AUTH_HEADER_PREFIX + token;
                ctx.putHeader(AUTH_HEADER_KEY, authHeaderVal);
                return HttpResponse.of("Authentication successful");
            });
        }

        if (config.failHandler == null) {
            builder.failHandler((ctx, e) -> HttpResponse.of(e.toString(), HttpURLConnection.HTTP_UNAUTHORIZED));
        }

        val finalConfig = builder.build();

        return AuthComponent.builder()
                .handleVerify(verifyHandler(finalConfig))
                .handleLogin(loginHandler(finalConfig))
                .build();
    }

    private static String extractToken(Context ctx) {
        val header = ctx.headerParam(AUTH_HEADER_KEY);
        return header.startsWith(AUTH_HEADER_PREFIX) ? header.substring(AUTH_HEADER_PREFIX.length()) : null;
    }

    private static Map<String, Object> verifyToken(SecretKey key, String token) {
        return Jwts.parserBuilder().setSigningKey(key)
                .build()
                .parseClaimsJws(token).getBody();
    }

    private static Handler verifyHandler(@NonNull Config config) {
        return ctx -> {
            try {
                val tokStr = extractToken(ctx);
                final Map<String, Object> claims;
                claims = verifyToken(config.secKeyObj, tokStr);
                val user = config.userRetriever.retrieve(ctx, claims);
                ctx.setDataParam(config.userKey, user);
                return HttpResponse.next();
            } catch (Exception e) {
                return config.failHandler.handle(ctx, e);
            }
        };
    }

    private static Handler loginHandler(@NonNull Config config) {
        return ctx -> {
            try {
                val userClaims = config.authenticator.authenticate(ctx);
                if (userClaims == null) {
                    throw new Exception("Authentication fail");
                }
                @SuppressWarnings("UseOfObsoleteDateTimeApi")
                val token = Jwts.builder()
                        .setHeaderParam("typ", "JWT")
                        .setIssuedAt(new Date())
                        .addClaims(userClaims)
                        .signWith(config.secKeyObj, SignatureAlgorithm.forName(config.algorithm))
                        .compact();
                return config.okHandler.handle(ctx, token, userClaims);
            } catch (Exception e) {
                return config.failHandler.handle(ctx, e);
            }
        };
    }

    /**
     * The interface User retriever.
     */
    @FunctionalInterface
    public interface UserRetriever {
        /**
         * Retrieve object.
         *
         * @param ctx    context object
         * @param claims the claims
         * @return the user data object
         */
        Object retrieve(Context ctx, Map<String, Object> claims);
    }

    /**
     * The interface Authenticator.
     */
    @FunctionalInterface
    public interface Authenticator {
        /**
         * Authenticate map.
         *
         * @param ctx context object
         * @return the map
         * @throws Exception the exception
         */
        Map<String, Object> authenticate(Context ctx) throws Exception;
    }

    /**
     * The interface Ok handler.
     */
    @FunctionalInterface
    public interface OkHandler {
        /**
         * Handle http response.
         *
         * @param ctx    context object
         * @param token  the token
         * @param claims the claims
         * @return the http response
         */
        HttpResponse handle(Context ctx, String token, Map<String, Object> claims);
    }

    /**
     * The interface Fail handler.
     */
    @FunctionalInterface
    public interface FailHandler {
        /**
         * Handle http response.
         *
         * @param ctx context object
         * @param e   exception
         * @return the http response
         */
        HttpResponse handle(Context ctx, Exception e);
    }

    /**
     * The type Auth component.
     */
    @Accessors(fluent = true)
    @Getter
    @Builder(toBuilder = true)
    public static class AuthComponent {
        /**
         * Handler that extracts user authentication info, authenticates user, then response with a JWT.
         * Put this on your login route.
         */
        private final Handler handleLogin;

        /**
         * Handler that extracts JWT, verifies it, retrieve user info with claims
         * then populate context with user info.
         * Put this on your restricted routes.
         */
        private final Handler handleVerify;
    }

    /**
     * The type Config.
     */
    @Getter
    @Builder(toBuilder = true)
    public static final class Config {
        /**
         * The constant USER_KEY_DEFAULT.
         */
        public static final String USER_KEY_DEFAULT = "authUser";
        /**
         * Secret key used for signing. Required and must be long enough. Recommend using {@link #genKey(String)}
         */
        private final String secretKey;

        private final SecretKey secKeyObj;

        /**
         * Algorithm to use in signing. Default: HS256
         */
        private final String algorithm;

        /**
         * Key used for getting data from context
         * Default: authUser
         */
        private final String userKey;

        /**
         * Function that retrieves user info based on context and token claims.
         * This data will be populated to context.
         * Default: returns {@code claims.get("sub")}
         */
        private final UserRetriever userRetriever;

        /**
         * Function that authenticates user given the Context object
         * and returns claims map to add to the JWT.
         * This will be used by the login handler. It should return {@code null} if authentication failed
         * Default: returns an empty HashMap
         */
        private final Authenticator authenticator;

        /**
         * Specifies action when authenticate success.
         * Default: set "Authentication: Bearer {token}" in response headers and returns 200;
         */
        private final OkHandler okHandler;

        /**
         * Specifies action when authenticate fail.
         * Default: returns 401 and exception message in body.
         */
        private final FailHandler failHandler;

        /**
         * ConfigBuilder for the Jwt middleware
         */
        // Override lombok builder to hide specific setters
        public static class ConfigBuilder {
            @SuppressWarnings("unused")
            private ConfigBuilder secKeyObj(SecretKey secKeyObj) {
                this.secKeyObj = secKeyObj;
                return this;
            }
        }
    }
}
