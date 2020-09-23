package com.tuhuynh.crud.entities;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseObject {
    String message;
    String error;
}
