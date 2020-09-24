package com.tuhuynh.jerrymouse.proxy;

import lombok.val;

import java.io.IOException;

public class TestProxy {
    public static void main(String[] args) throws IOException {
        val proxy = Proxy.port(8000);
        proxy.use("/cat-app", "localhost:1234");
        proxy.use("/dog-app", "localhost:1235");
        proxy.start();
    }
}
