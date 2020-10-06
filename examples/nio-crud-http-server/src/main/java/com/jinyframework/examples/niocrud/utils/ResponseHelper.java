package com.jinyframework.examples.niocrud.utils;

import com.jinyframework.core.RequestBinderBase.HttpResponse;
import com.jinyframework.examples.niocrud.entities.ResponseEntity;
import lombok.val;

import java.util.concurrent.CompletableFuture;

public final class ResponseHelper {
    public static CompletableFuture<HttpResponse> success(String msg) {
        val obj = ResponseEntity.builder().message(msg).build();
        return HttpResponse.ofAsync(obj);
    }

    public static CompletableFuture<HttpResponse> error(String msg) {
        val obj = ResponseEntity.builder().error(msg).build();
        return HttpResponse.ofAsync(obj);
    }
}
