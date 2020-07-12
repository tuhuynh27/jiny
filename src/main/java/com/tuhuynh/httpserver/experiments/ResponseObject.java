package com.tuhuynh.httpserver.experiments;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class ResponseObject {
    private final String status;
    private final String threadName;

    @Override
    public String toString() {
        return "{\"status\":\"" + status + "\",\"thread_name\":\"" + threadName + "\"}";
    }
}