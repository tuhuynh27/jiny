package com.yourcompany;

import com.jinyframework.*;
import static com.jinyframework.core.AbstractRequestBinder.HttpResponse.of;

import com.google.gson.Gson;

import lombok.val;

public class App {
    public static void main(String[] args) {
		val server = HttpServer.port(1234);
		
		
		
        server.get("/hello", ctx -> of("Hello World!"));
        server.start();
    }
}
