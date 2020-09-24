package com.tuhuynh.jerrymouse;

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
    @DisplayName("Hello World test")
    void helloWorld() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "Hello World");
    }

    @Test
    @DisplayName("Echo test")
    void echo() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/echo").method("POST").body("copycat")
                .build().perform();
        assertEquals(res.getBody(), "copycat");
    }

    @Test
    @DisplayName("Query Params test")
    void queryParams() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/query?hello=world").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "world");
    }

    @Test
    @DisplayName("Path Params test")
    void pathParams() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/path/hello/123").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "hello:123");
    }

    @Test
    @DisplayName("Catch All test")
    void catchAll() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/all/123/456").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "/all/123/456");
    }

    @Test
    @DisplayName("Middleware test failed")
    void middlewareFail() throws IOException {
        val res = HttpClient.builder()
                .url(url + "/protected").method("GET")
                .build().perform();
        assertEquals(res.getStatus(), 401);
        assertEquals(res.getBody(), "invalid_token");
    }

    @Test
    @DisplayName("Middleware test success")
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
                .url(url + "/gm").method("GET")
                .build().perform();
        assertEquals(res.getBody(), "middleware");
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
        val res = HttpClient.builder()
                .url(url + "/cat/echo").method("POST").body("copycat")
                .build().perform();
        assertEquals(res.getBody(), "copycat");
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
        if (isCI) {
            TimeUnit.SECONDS.sleep(1);
        } else {
            TimeUnit.MILLISECONDS.sleep(200);
        }
    }
}
