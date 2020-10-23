package com.jinyframework.middlewares;

import com.jinyframework.core.AbstractRequestBinder.Context;
import com.jinyframework.core.AbstractRequestBinder.Handler;
import com.jinyframework.middlewares.Cors.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("middleware.Cors")
public class CorsTest {
    static final String uri = "http://localhost";
    @Test
    @DisplayName("Allow all")
    void allowAll() throws Exception {
        final Handler handler = Cors.newHandler(Config.builder().allowAll(true).build());
        final Map<String, String> reqHeaders = new HashMap<>();
        reqHeaders.put("Origin".toLowerCase(), uri);
        final Context ctx = Context.builder()
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
        final Handler handler = Cors.newHandler(Config.builder()
                .allowAll(false)
                .allowOrigin(uri)
                .build());
        final Map<String, String> reqHeaders = new HashMap<>();
        reqHeaders.put("Origin".toLowerCase(), uri);
        final Context successCtx = Context.builder()
                .header(reqHeaders)
                .responseHeaders(new HashMap<>())
                .build();
        handler.handleFunc(successCtx);
        assertEquals(successCtx.getResponseHeaders().get("Vary"), "Origin");
        assertEquals(successCtx.getResponseHeaders().get("Access-Control-Allow-Origin"), uri);

        reqHeaders.put("Origin".toLowerCase(), "http://wronghost");
        final Context failCtx = Context.builder()
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
        final Handler handler = Cors.newHandler(Config.builder()
                                                      .allowAll(false)
                                                      .allowOrigin(uri)
                                                      .allowCredentials(true)
                                                      .build());
        final Map<String, String> reqHeaders = new HashMap<>();
        reqHeaders.put("Origin".toLowerCase(), uri);
        final Context successCtx = Context.builder()
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
        final Handler handler = Cors.newHandler(Config.builder()
                                                      .allowAll(false)
                                                      .allowOrigin(uri)
                                                      .exposeHeader("Foo")
                                                      .build());
        final Map<String, String> reqHeaders = new HashMap<>();
        reqHeaders.put("Origin".toLowerCase(), uri);
        final Context successCtx = Context.builder()
                                          .header(reqHeaders)
                                          .responseHeaders(new HashMap<>())
                                          .build();
        handler.handleFunc(successCtx);
        assertEquals(successCtx.getResponseHeaders().get("Vary"), "Origin");
        assertEquals(successCtx.getResponseHeaders().get("Access-Control-Allow-Origin"), uri);
        assertEquals(successCtx.getResponseHeaders().get("Access-Control-Expose-Headers"), "Foo");
    }
}