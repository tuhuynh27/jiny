package com.jinyframework.examples.crud.utils;

import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.examples.crud.entities.ResponseEntity;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class ResponseHelper {
    public HttpResponse success(String msg) {
        val obj = ResponseEntity.builder().message(msg).build();
        return HttpResponse.of(obj);
    }

    public HttpResponse error(String msg) {
        val obj = ResponseEntity.builder().error(msg).build();
        return HttpResponse.of(obj);
    }
}
