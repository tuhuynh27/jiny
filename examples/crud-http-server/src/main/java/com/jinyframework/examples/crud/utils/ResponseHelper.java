package com.jinyframework.examples.crud.utils;

import com.jinyframework.core.RequestBinderBase.HttpResponse;
import com.jinyframework.examples.crud.entities.ResponseEntity;
import lombok.val;

public final class ResponseHelper {
    public static HttpResponse success(String msg) {
        val obj = ResponseEntity.builder().message(msg).build();
        return HttpResponse.of(obj);
    }

    public static HttpResponse error(String msg) {
        val obj = ResponseEntity.builder().error(msg).build();
        return HttpResponse.of(obj);
    }
}
