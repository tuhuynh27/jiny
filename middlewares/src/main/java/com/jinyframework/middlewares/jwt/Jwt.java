package com.jinyframework.middlewares.jwt;

import com.jinyframework.core.AbstractRequestBinder.Handler;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

public final class Jwt {

    @Accessors(fluent = true)
    @Getter
    @Builder
    public static class AuthComponent {
        private final Handler permissionHandler;
        private final Handler refreshHandler;
        private final Handler loginHandler;
        private final Handler ctxHandler;
    }

    public static AuthComponent newAuthComponent(@NonNull Config config) {
        return AuthComponent.builder()
                .build();
    }

    @Getter
    @Builder(toBuilder = true)
    public static final class Config {
        /**
         * Secret key used for signing. Required, will throw exception if missing.
         * */
        private final String secretKey;
    }
}
