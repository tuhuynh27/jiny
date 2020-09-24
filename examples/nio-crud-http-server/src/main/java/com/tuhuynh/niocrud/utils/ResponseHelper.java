package com.tuhuynh.niocrud.utils;

import com.google.gson.Gson;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.HttpResponse;
import com.tuhuynh.niocrud.entities.ResponseEntity;
import lombok.val;

import java.util.concurrent.CompletableFuture;

public final class ResponseHelper {
    public static CompletableFuture<HttpResponse> success(String msg) {
        val gson = new Gson();
        val obj = ResponseEntity.builder().message(msg).build();
        return HttpResponse.ofAsync(obj, gson::toJson);
    }

    public static CompletableFuture<HttpResponse> error(String msg) {
        val gson = new Gson();
        val obj = ResponseEntity.builder().error(msg).build();
        return HttpResponse.ofAsync(obj, gson::toJson);
    }
}
