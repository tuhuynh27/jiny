package com.jinyframework;

import lombok.val;

import java.io.IOException;

public final class Test {
    public static void main(String[] args) throws IOException {
        val proxy = NIOHttpProxy.port(1234);
        proxy.use("/server1", "localhost:1111");
        proxy.use("/server2", "linecorp.com:80");
        proxy.start();
    }
}
