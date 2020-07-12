package com.tuhuynh.httpserver.tests;

import java.io.IOException;

import lombok.val;

import com.tuhuynh.httpserver.HTTPClient;

public final class TestClient {
    public static void main(String[] args) throws IOException {
        val result = HTTPClient.builder().url("https://tuhuynh.com").method("GET").build().perform();
        System.out.println(result);
    }
}
