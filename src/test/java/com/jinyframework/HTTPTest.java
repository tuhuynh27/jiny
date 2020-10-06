package com.jinyframework;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class HTTPTest {
    protected String url = "";
    protected boolean isCI =
            System.getenv("CI") != null
                    && System.getenv("CI").toLowerCase().equals("true");

    @Test
    @DisplayName("Hello World")
    void helloWorld() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "Hello World");
    }

    @Test
    @DisplayName("Transformer")
    void transformer() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/transform").method("POST").body("transform")
                .build().perform();
        assertEquals(res.getBody(), "transformed");
    }

    @Test
    @DisplayName("Echo")
    void echo() throws IOException {
        if (isCI) return;

        val res = HttpClient.builder()
                .url(url + "/echo").method("POST").body("copycat")
                .build().perform();
        assertEquals(res.getBody(), "copycat");
    }

    @Test
    @DisplayName("Response Header")
    void header() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/header").method("GET")
                .build().perform();
        assertEquals(res.getHeader("foo"), "bar");
    }

    @Test
    @DisplayName("Query Params")
    void queryParams() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/query?hello=world").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "world");
    }

    @Test
    @DisplayName("Path Params")
    void pathParams() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/path/hello/123").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "hello:123");
    }

    @Test
    @DisplayName("Catch All")
    void catchAll() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/all/123/456").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "/all/123/456");
    }

    @Test
    @DisplayName("Middleware failed")
    void middlewareFail() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/protected").method("GET")
                .build().perform();
        assertEquals(res.getStatus(), 401);
        assertEquals(res.getBody(), "invalid_token");
    }

    @Test
    @DisplayName("Middleware success")
    void middlewareSuccess() throws IOException {
        val headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer test_token");
        val res = HttpClient.builder()
                .url(url + "/protected").method("GET").headers(headers)
                .build().perform();
        assertEquals(res.getStatus(), 200);
        assertEquals(res.getBody(), "success:tuhuynh");
    }

    @Test
    @DisplayName("Panic")
    void panic() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/panic").method("GET")
                .build().perform();
        assertEquals(res.getStatus(), 500);
        assertEquals(res.getBody(), "Panicked!");
    }

    @Test
    @DisplayName("Global Middleware")
    void globalMiddleware() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/gm").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "middleware");
    }

    @Test
    @DisplayName("Global Middleware not from subRouter")
    void globalMiddlewareNotFromSubRouter() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/gm-sub").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "");
    }

    @Test
    @DisplayName("SubRouter1")
    void subRouter1() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/cat").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "this is a cat");
    }

    @Test
    @DisplayName("SubRouter2")
    void subRouter2() throws IOException {
        if (isCI) return;

        val res = HttpClient.builder()
                .url(url + "/cat/test").method("POST")
                .body("catTest")
                .build().perform();
        assertEquals(res.getBody(), "catTest");
    }

    @Test
    @DisplayName("SubRouter3")
    void subRouter3() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/cat/gm").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "cat");
    }

    @Test
    @DisplayName("SubRouter4")
    void subRouter4() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/cat/foo/bar").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "foo:bar");
    }

    @BeforeEach
    void each() throws InterruptedException {
        // Do something on CI if (isCI)
        // TimeUnit.SECONDS.sleep(1);
        // No sleep

        if (isCI) {
            TimeUnit.MILLISECONDS.sleep(1000);
        }
    }
}
