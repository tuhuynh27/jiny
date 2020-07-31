package com.tuhuynh.httpserver.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tuhuynh.httpserver.core.ParserUtils.RequestMethod;
import com.tuhuynh.httpserver.core.RequestBinder.HttpResponse;
import com.tuhuynh.httpserver.core.RequestBinder.RequestContext;

public class ParserUtilsTest {
    @Test
    @DisplayName("Parse Request Test")
    void parseRequestTest() {
        final String[] request = { "GET /test HTTP/1.1", "Host: localhost", "User-Agent: Mozilla/5.0" };
        final String body = "SampleBody";
        final RequestContext context = ParserUtils.parseRequest(request, body);
        assertEquals("/test", context.getPath(), "Get Path");
        assertEquals(RequestMethod.GET, context.getMethod(), "Get Method");
        assertEquals("Mozilla/5.0", context.getHeader().get("user-agent"), "Get Header");
        assertEquals("SampleBody", context.getBody(), "Get Body");
    }

    @Test
    @DisplayName("Parse Response Test")
    void parseResponseTest() {
        final HttpResponse response = HttpResponse.of("Hello World");
        assertEquals(200, response.getHttpStatusCode(), "Get HTTP Status Code");
        final String responseString = ParserUtils.parseResponse(response);
        assertEquals("HTTP/1.1 200 OK\n\nHello World\n", responseString, "Get response string");
    }
}
