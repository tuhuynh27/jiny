package com.jinyframework.middlewares.jwt;

import com.jinyframework.HttpClient;
import com.jinyframework.HttpServer;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.jinyframework.core.AbstractRequestBinder.HttpResponse.of;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("middlewares.jwt.Jwt")
public class JwtTest {
    private static final HttpServer server = HttpServer.port(1234);
    private static final String url = "http://localhost:1234";

    @BeforeAll
    static void startServer() throws InterruptedException {
        new Thread(() -> {
            val secretKey = Jwt.genKey("HS256");
            Jwt.AuthComponent authComponent = Jwt.newAuthComponent(Jwt.Config.builder()
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
        }).start();

        // Wait for server to start
        TimeUnit.SECONDS.sleep(3);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("Login Handler")
    void login() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/login").method("POST")
                .build().perform();
        assertNotNull(res.getHeader("Authentication"));
    }

    @Test
    @DisplayName("Check Token")
    void checkToken() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/path").method("GET")
                .build().perform();
        assertNotNull(res.getBody());
    }
}
