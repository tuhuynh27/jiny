package com.tuhuynh.jerrymouse.tests;

import java.io.IOException;

import com.tuhuynh.jerrymouse.HttpClient;

import lombok.val;

public final class TestClient {
    public static void main(String[] args) throws IOException {
        val result = HttpClient.builder().url("https://tuhuynh.com").method("GET").build().perform();
        System.out.println(result.getBody());
    }
}
