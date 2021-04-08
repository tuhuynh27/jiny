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
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jinyframework.core.AbstractRequestBinder.HttpResponse.of;
import static com.jinyframework.core.AbstractRequestBinder.HttpResponse.reject;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("middlewares.jwt.Jwt")
class JwtTest {
    private static final HttpServer server = HttpServer.port(1235);
    private static final String url = "http://localhost:1235";

    @BeforeAll
    static void startServer() throws InterruptedException {
        new Thread(() -> {
            val secretKey = Jwt.genKey("HS256");
            final Jwt.AuthComponent authComponent = Jwt.newAuthComponent(Jwt.Config.builder()
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

            server.get("/claims", authComponent.handleVerify(),
                    ctx -> {
                        @SuppressWarnings("unchecked")
                        val claims = (Map<String, Object>) ctx.dataParam(Jwt.Config.USER_KEY_DEFAULT);
                        if (!claims.keySet()
                                .containsAll(Stream.of("aud", "sub", "iss", "iat").collect(Collectors.toList()))) {
                            return reject("reject");
                        }
                        return of("ok");
                    });

            val hs512Key = Jwt.genKey("HS512");
            final Jwt.AuthComponent algoCase = Jwt.newAuthComponent(Jwt.Config.builder()
                    .secretKey(hs512Key)
                    .algorithm("HS512")
                    .build());
            server.post("/algo-login", algoCase.handleLogin());
            server.get("/algo-verify", algoCase.handleVerify(), ctx -> of("ok"));

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
        val res = performLogin();
        val header = res.getHeader(Jwt.AUTH_HEADER_KEY);
        assertNotNull(header);
        assertFalse(header.isEmpty());
    }

    HttpClient.ResponseObject performLogin() throws IOException {
        return HttpClient.builder()
                .url(url + "/login").method("POST")
                .build().perform();
    }

    @Test
    @DisplayName("Check Token")
    void checkToken() throws IOException {
        val loginRes = performLogin();
        val res = HttpClient.builder()
                .url(url + "/path").method("GET")
                .header(Jwt.AUTH_HEADER_KEY, loginRes.getHeader(Jwt.AUTH_HEADER_KEY))
                .build().perform();
        assertEquals(200, res.getStatus());
        assertNotNull(res.getBody());
    }

    @Test
    @DisplayName("Check data param and claims")
    void checkClaims() throws IOException {
        val loginRes = performLogin();
        val res = HttpClient.builder()
                .url(url + "/claims").method("GET")
                .header(Jwt.AUTH_HEADER_KEY, loginRes.getHeader(Jwt.AUTH_HEADER_KEY))
                .build().perform();
        assertEquals(200, res.getStatus());
    }

    @Test
    @DisplayName("Check algo config")
    void checkAlgo() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/algo-login").method("POST")
                .build().perform();
        val authHeader = res.getHeader(Jwt.AUTH_HEADER_KEY);
        assertEquals(200, res.getStatus());
        assertNotNull(authHeader);
        val resVerify = HttpClient.builder()
                .url(url + "/algo-verify").method("GET")
                .header(Jwt.AUTH_HEADER_KEY, authHeader)
                .build().perform();
        assertEquals(200, resVerify.getStatus());
    }
}
