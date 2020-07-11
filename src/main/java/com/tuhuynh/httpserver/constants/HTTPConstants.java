package com.tuhuynh.httpserver.constants;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class HTTPConstants {
    public static final String HTTP_HEADER_OK = "HTTP/1.1 200 OK\n\n";
    public static final String HTTP_HEADER_NOT_FOUND = "HTTP/1.1 404\n\n" + "{\"message\": \"404\"}";
}
