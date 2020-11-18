package com.jinyframework.middlewares.jwt;

import com.jinyframework.HttpClient;
import com.jinyframework.HttpServer;
import lombok.val;

import static com.jinyframework.core.AbstractRequestBinder.HttpResponse.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class JwtTest {
    private static final HttpServer server = HttpServer.port(1234);
    private static final String url = "http://localhost:1234";

    @BeforeAll
    static void startServer() throws InterruptedException {
        val secretKey = Jwt.genKey("HS256");
        val authComponent = Jwt.newAuthComponent(Jwt.Config.builder()
                .secretKey(secretKey)
                .authenticator(ctx -> {
                    val claims = new HashMap<String, Object>();
                    claims.put("aud", "client");
                    claims.put("sub", "userName");
                    claims.put("iss", "host");
                    return claims;
                })
                .userRetriever((ctx, claims) -> claims)
                .build());
        server.post("/login", authComponent.handleLogin());
        server.get("/path", authComponent.handleVerify(),
                ctx -> of(ctx.dataParam(Jwt.Config.USER_KEY_DEFAULT)));
        server.start();

        // Wait for server to start
        TimeUnit.SECONDS.sleep(3);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("Login Handler")
    void defaultResponseHeaders() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/").method("GET")
                .build().perform();
        assertEquals(res.getHeader("Content-Type"), "application/json");
    }
}
