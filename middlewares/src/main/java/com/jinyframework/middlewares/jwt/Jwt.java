package com.jinyframework.middlewares.jwt;

import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.*;
import lombok.experimental.Accessors;

import javax.crypto.SecretKey;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.jinyframework.core.AbstractRequestBinder.Context;

public final class Jwt {

    public static final String AUTH_HEADER_KEY = "Authorization";
    public static final String AUTH_HEADER_PREFIX = "Bearer ";

    private Jwt() {
    }

    /**
     * Generate a HS256 key string. You should store this somewhere safe for reuse.
     * */
    public static String genHS256Key() {
        return Arrays.toString(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());
    }

    @SneakyThrows
    public static AuthComponent newAuthComponent(@NonNull Config config) {
        if (config.secretKey == null) {
            throw new Exception("Secret key not provided");
        }
        val builder = config.toBuilder();

        builder.secKeyObj(Keys.hmacShaKeyFor(config.secretKey.getBytes(StandardCharsets.UTF_8)));

        if (config.userKey == null) {
            builder.userKey(Config.USER_KEY_DEFAULT);
        }
        if (config.userRetriever == null) {
            builder.userRetriever((ctx,claims)-> String.valueOf(claims.get(Claims.SUBJECT)));
        }

        if (config.authenticator == null) {
            builder.authenticator(ctx-> new HashMap<>());
        }

        if (config.okHandler == null) {
            builder.okHandler((ctx,token,claims)->HttpResponse.of("Authentication successful"));
        }

        if (config.failHandler == null) {
            builder.failHandler((ctx,e)->HttpResponse.of(e.toString(), HttpURLConnection.HTTP_UNAUTHORIZED));
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
                return config.failHandler.handle(ctx,e);
            }
        };
    }

    private static Handler loginHandler(@NonNull Config config) {
        return ctx -> {
            try {
                val claims = config.authenticator.authenticate(ctx);
                if (claims == null) {
                    throw new Exception("Authentication fail");
                }
                val token = Jwts.builder()
                        .addClaims(claims)
                        .signWith(config.secKeyObj)
                        .compact();
                val authHeaderVal = AUTH_HEADER_PREFIX + token;
                ctx.putHeader(AUTH_HEADER_KEY, authHeaderVal);
                return config.okHandler.handle(ctx,token,claims);
            } catch (Exception e) {
                return config.failHandler.handle(ctx,e);
            }
        };
    }

    @FunctionalInterface
    public interface UserRetriever {
        Object retrieve(Context ctx, Map<String,Object> claims);
    }

    @FunctionalInterface
    public interface Authenticator {
        Map<String,Object> authenticate(Context ctx) throws Exception;
    }

    @FunctionalInterface
    public interface OkHandler {
        HttpResponse handle(Context ctx, String token, Map<String,Object> claims);
    }

    @FunctionalInterface
    public interface FailHandler {
        HttpResponse handle(Context ctx, Exception e);
    }

    @Accessors(fluent = true)
    @Getter
    @Builder(toBuilder = true)
    public static class AuthComponent {
        /**
         * Handler that extracts user authentication info, authenticates user, then response with a JWT
         */
        private final Handler handleLogin;

        /**
         * Handler that extracts JWT, verifies it, retrieve user info with claims
         * then populate context with user info.
         */
        private final Handler handleVerify;
    }

    @Getter
    @Builder(toBuilder = true)
    public static final class Config {
        public static final String USER_KEY_DEFAULT = "authUser";
        /**
         * Secret key used for signing. Required and must be long enough. Recommend using {@link #genHS256Key()}
         */
        private final String secretKey;
        private final SecretKey secKeyObj;

        /**
         * Algorithm to use in signing. Default: HS256
         * */
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

        private final OkHandler okHandler;
        private final FailHandler failHandler;

        public static class ConfigBuilder {
            @SuppressWarnings("unused")
            private ConfigBuilder secKeyObj(SecretKey secKeyObj) {
                return this;
            }
        }
    }
}
