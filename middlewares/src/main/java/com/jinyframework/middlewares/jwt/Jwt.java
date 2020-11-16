package com.jinyframework.middlewares.jwt;

import com.jinyframework.core.AbstractRequestBinder;
import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
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

public final class Jwt {

    private static final String authHeaderKey = "Authorization";
    private static final String authHeaderPrefix = "Bearer ";

    private Jwt() {
    }

    public static String genHS256Key() {
        return Arrays.toString(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());
    }

    @SneakyThrows
    public static AuthComponent newAuthComponent(@NonNull Config config) {
        if (config.secretKey == null) {
            throw new Exception("Secret key not provided");
        }
        val builder = config.toBuilder();

        val secKeyObj = Keys.hmacShaKeyFor(config.secretKey.getBytes(StandardCharsets.UTF_8));
        builder.secKeyObj(secKeyObj);

        if (config.userKey == null) {
            builder.userKey(Config.userKeyDefault);
        }
        if (config.userRetriever == null) {
            final UserRetriever retriever = (ctx, claims) -> String.valueOf(claims.get(Claims.SUBJECT));
            builder.userRetriever(retriever);
        }

        if (config.authenticator == null) {
            final Authenticator authenticator = ctx -> new HashMap<>();
            builder.authenticator(authenticator);
        }
        val finalConfig = builder.build();

        return AuthComponent.builder()
                .handleVerify(verifyHandler(finalConfig))
                .handleLogin(loginHandler(finalConfig))
                .build();
    }

    private static String extractToken(AbstractRequestBinder.Context ctx) {
        val header = ctx.headerParam(authHeaderKey);
        return header.startsWith(authHeaderPrefix) ? header.substring(authHeaderPrefix.length()) : null;
    }

    private static Map<String, Object> verifyToken(SecretKey key, String token) {
        return Jwts.parserBuilder().setSigningKey(key)
                .build()
                .parseClaimsJws(token).getBody();
    }

    private static Handler verifyHandler(@NonNull Config config) {
        return ctx -> {
            val tokStr = extractToken(ctx);
            if (tokStr == null) {
                return HttpResponse.reject("Authentication failed", HttpURLConnection.HTTP_UNAUTHORIZED);
            }
            final Map<String, Object> claims;
            try {
                claims = verifyToken(config.secKeyObj, tokStr);
            } catch (Exception e) {
                return HttpResponse.reject(e.toString(), HttpURLConnection.HTTP_UNAUTHORIZED);
            }
            val user = config.userRetriever.retrieveUser(ctx, claims);
            ctx.setDataParam(config.userKey, user);
            return HttpResponse.next();
        };
    }

    private static Handler loginHandler(@NonNull Config config) {
        return ctx -> {
            val claims = config.authenticator.authenticate(ctx);
            if (claims == null) {
                return HttpResponse.reject("Authentication failed", HttpURLConnection.HTTP_UNAUTHORIZED);
            }
            val token = Jwts.builder()
                    .addClaims(claims)
                    .signWith(config.secKeyObj)
                    .compact();
            val authHeaderVal = authHeaderPrefix + token;
            ctx.getResponseHeaders().put(authHeaderKey, authHeaderVal);
            return HttpResponse.of("Authentication successful");
        };
    }

    @FunctionalInterface
    public interface UserRetriever {
        Object retrieveUser(AbstractRequestBinder.Context ctx, Map<String, Object> claims);
    }

    @FunctionalInterface
    public interface Authenticator {
        Map<String, Object> authenticate(AbstractRequestBinder.Context ctx) throws Exception;
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    public interface Claims extends io.jsonwebtoken.Claims {

    }

    @Accessors(fluent = true)
    @Getter
    @Builder(toBuilder = true)
    public static class AuthComponent {
        private final Handler handlePermission;
        private final Handler handleRefresh;
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
        public static final String userKeyDefault = "authUser";
        /**
         * Secret key used for signing. Required and must be long enough. Recommend using {@link #genHS256Key()}
         */
        private final String secretKey;
        private final SecretKey secKeyObj;
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
         * This will be used by the login handler. It should return null if authentication fails
         * Default: returns an empty HashMap
         */
        private final Authenticator authenticator;

        public static class ConfigBuilder {
            private ConfigBuilder secKeyObj(SecretKey secKeyObj) {
                return this;
            }
        }
    }
}
