package com.jinyframework.middlewares.jwt;

import com.jinyframework.core.AbstractRequestBinder;
import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.Map;

public final class Jwt {

    private Jwt() {
    }

    @Accessors(fluent = true)
    @Getter
    @Builder
    public static class AuthComponent {
        private final Handler handlePermission;
        private final Handler handleRefresh;
        private final Handler handleLogin;
        private final Handler handleVerify;
    }

    public static AuthComponent newAuthComponent(@NonNull Config config) throws Exception {
        if (config.secretKey == null) {
            throw new Exception("Secret key not provided");
        }
        val builder = config.toBuilder();

        if (config.userRetriever == null) {
            final UserRetriever<String> retriever = (ctx, claims) -> String.valueOf(claims.get(Claims.SUBJECT));
            builder.userRetriever(retriever);
        }

        val finalConfig = builder.build();

        return AuthComponent.builder()
                .handleVerify(verifyHandler(finalConfig))
                .build();
    }

    private static final String authHeaderKey = "Authorization";
    private static final String authHeaderPrefix = "Bearer ";

    private static String extractToken(AbstractRequestBinder.Context ctx) {
        val header = ctx.headerParam(authHeaderKey);
        return header.startsWith(authHeaderPrefix) ? header.substring(authHeaderPrefix.length()) : header;
    }

    private static Map<String, Object> verifyToken(String key, String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    private static Handler verifyHandler(@NonNull Config config) {
        return ctx -> {
            val tokStr = extractToken(ctx);
            val claims = verifyToken(config.secretKey, tokStr);
            val user = config.userRetriever.retrieveUser(ctx, claims);
            ctx.setDataParam(config.userKey, user);
            return HttpResponse.next();
        };
    }

    @FunctionalInterface
    public interface UserRetriever<T> {
        T retrieveUser(AbstractRequestBinder.Context ctx, Map<String, Object> claims);
    }

    @Getter
    @Builder(toBuilder = true)
    public static final class Config {
        /**
         * Secret key used for signing. Required.
         */
        private final String secretKey;

        /**
         * Key used for getting data from context
         */
        private final String userKey;

        /**
         * Function that retrieves user info based on context and token claims
         * Default: returns {@code claims.get("sub")}
         */
        @SuppressWarnings("rawtypes")
        private final UserRetriever userRetriever;
    }
}
