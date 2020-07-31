package com.tuhuynh.httpserver.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tuhuynh.httpserver.core.ParserUtils.RequestMethod;
import com.tuhuynh.httpserver.core.RequestBinder.HttpResponse;

import lombok.val;

public class ParserUtilsTest {
    @Test
    @DisplayName("Parse Request Test")
    void parseRequestTest() {
        final String[] request = { "GET /test HTTP/1.1", "Host: localhost", "User-Agent: Mozilla/5.0" };
        val body = "SampleBody";
        val context = ParserUtils.parseRequest(request, body);
        assertEquals("/test", context.getPath(), "Get Path");
        assertEquals(RequestMethod.GET, context.getMethod(), "Get Method");
        assertEquals("Mozilla/5.0", context.getHeader().get("user-agent"), "Get Header");
        assertEquals("SampleBody", context.getBody(), "Get Body");
    }

    @Test
    @DisplayName("Parse Response Test")
    void parseResponseTest() {
        val response = HttpResponse.of("Hello World");
        assertEquals(200, response.getHttpStatusCode(), "Get HTTP Status Code");
        val responseString = ParserUtils.parseResponse(response);
        assertEquals("HTTP/1.1 200 OK\n\nHello World\n", responseString, "Get response string");
    }
}
