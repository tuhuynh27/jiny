package com.jinyframework.middlewares.cors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jinyframework.core.AbstractRequestBinder.Context;
import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;
import com.jinyframework.middlewares.cors.Cors.Config;

import lombok.val;

@DisplayName("middleware.cors.Cors")
public class CorsTest {
    static final String uri = "http://localhost";

    @Test
    @DisplayName("Allow all")
    void allowAll() throws Exception {
        val handler = Cors.newHandler(Config.builder()
                                            .allowAllOrigins(true).build());
        val reqHeaders = new HashMap<String, String>();
        reqHeaders.put("Origin".toLowerCase(), uri);
        val ctx = Context.builder()
                         .header(reqHeaders)
                         .responseHeaders(new HashMap<>())
                         .build();
        handler.handleFunc(ctx);
        assertEquals(ctx.getResponseHeaders().get("Vary"), "Origin");
        assertEquals(ctx.getResponseHeaders().get("Access-Control-Allow-Origin"), "*");
    }

    @Test
    @DisplayName("Allow from list")
    void allowFromList() throws Exception {
        val handler = Cors.newHandler(Config.builder()
                                            .allowAllOrigins(false)
                                            .allowOrigin(uri)
                                            .build());
        val reqHeaders = new HashMap<String, String>();
        reqHeaders.put("Origin".toLowerCase(), uri);
        val successCtx = Context.builder()
                                .header(reqHeaders)
                                .responseHeaders(new HashMap<>())
                                .build();
        handler.handleFunc(successCtx);
        assertEquals(successCtx.getResponseHeaders().get("Vary"), "Origin");
        assertEquals(successCtx.getResponseHeaders().get("Access-Control-Allow-Origin"), uri);

        reqHeaders.put("Origin".toLowerCase(), "http://wronghost");
        val failCtx = Context.builder()
                             .header(reqHeaders)
                             .responseHeaders(new HashMap<>())
                             .build();
        handler.handleFunc(failCtx);
        assertEquals(failCtx.getResponseHeaders().get("Vary"), "Origin");
        assertNull(failCtx.getResponseHeaders().get("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Allow credentials")
    void allowCredentials() throws Exception {
        val handler = Cors.newHandler(Config.builder()
                                            .allowAllOrigins(false)
                                            .allowOrigin(uri)
                                            .allowCredentials(true)
                                            .build());
        val reqHeaders = new HashMap<String, String>();
        reqHeaders.put("Origin".toLowerCase(), uri);
        val successCtx = Context.builder()
                                .header(reqHeaders)
                                .responseHeaders(new HashMap<>())
                                .build();
        handler.handleFunc(successCtx);
        assertEquals(successCtx.getResponseHeaders().get("Vary"), "Origin");
        assertEquals(successCtx.getResponseHeaders().get("Access-Control-Allow-Origin"), uri);
        assertEquals(successCtx.getResponseHeaders().get("Access-Control-Allow-Credentials"), "true");
    }

    @Test
    @DisplayName("Expose headers")
    void exposeHeaders() throws Exception {
        val handler = Cors.newHandler(Config.builder()
                                            .allowAllOrigins(false)
                                            .allowOrigin(uri)
                                            .exposeHeader("Foo")
                                            .build());
        val reqHeaders = new HashMap<String, String>();
        reqHeaders.put("Origin".toLowerCase(), uri);
        val successCtx = Context.builder()
                                .header(reqHeaders)
                                .responseHeaders(new HashMap<>())
                                .build();
        handler.handleFunc(successCtx);
        assertEquals(successCtx.getResponseHeaders().get("Vary"), "Origin");
        assertEquals(successCtx.getResponseHeaders().get("Access-Control-Allow-Origin"), uri);
        assertEquals(successCtx.getResponseHeaders().get("Access-Control-Expose-Headers"), "Foo");
    }

    @Test
    @DisplayName("Request + Allow methods")
    void requestAllowMethods() throws Exception {
        val handler = Cors.newHandler(Config.builder()
                                            .allowAllOrigins(false)
                                            .allowOrigin(uri)
                                            .allowMethod("PUT")
                                            .build());
        val reqHeaders = new HashMap<String, String>();
        reqHeaders.put("Origin".toLowerCase(), uri);
        reqHeaders.put("Access-Control-Request-Method".toLowerCase(),"PUT");
        val successCtx = Context.builder()
                                .method(HttpMethod.OPTIONS)
                                .header(reqHeaders)
                                .responseHeaders(new HashMap<>()).build();
        val successRes = handler.handleFunc(successCtx);
        assertTrue(successCtx.getResponseHeaders().get("Access-Control-Allow-Methods").contains("PUT"));
        assertEquals(successRes.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    @DisplayName("Request + Allow headers")
    void requestAllowHeaders() throws Exception {
        val handler = Cors.newHandler(Config.builder()
                                            .allowAllOrigins(false)
                                            .allowOrigin(uri)
                                            .allowHeader("Bar")
                                            .build());
        val reqHeaders = new HashMap<String, String>();
        reqHeaders.put("Origin".toLowerCase(), uri);
        reqHeaders.put("Access-Control-Request-Method".toLowerCase(),"GET");
        reqHeaders.put("Access-Control-Request-Headers".toLowerCase(),"Bar");
        val successCtx = Context.builder()
                                .method(HttpMethod.OPTIONS)
                                .header(reqHeaders)
                                .responseHeaders(new HashMap<>()).build();
        val successRes = handler.handleFunc(successCtx);
        assertTrue(successCtx.getResponseHeaders().get("Access-Control-Allow-Headers").contains("Bar"));
        assertEquals(successRes.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    @DisplayName("Default settings for empty config fields")
    void defaultSettingEmptyFields() throws Exception {
        final Handler handler = Cors.newHandler(Config.builder()
                                                      .allowOrigin(uri)
                                                      .build());
        final Map<String, String> reqHeaders = new HashMap<>();
        reqHeaders.put("Origin".toLowerCase(), uri);
        reqHeaders.put("Access-Control-Request-Method".toLowerCase(),"HEAD");
        final Context ctx = Context.builder()
                                   .method(HttpMethod.OPTIONS)
                                   .header(reqHeaders)
                                   .responseHeaders(new HashMap<>())
                                   .build();
        handler.handleFunc(ctx);
        assertTrue(ctx.getResponseHeaders().get("Access-Control-Allow-Headers").contains("Content-Type"));
        assertTrue(ctx.getResponseHeaders().get("Access-Control-Allow-Headers").contains("Origin"));
        assertTrue(ctx.getResponseHeaders().get("Access-Control-Allow-Methods").contains("HEAD"));
        assertEquals(ctx.getResponseHeaders().get("Access-Control-Allow-Origin"), uri);
    }
}
