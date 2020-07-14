package com.tuhuynh.httpserver.core.nio;

import java.util.concurrent.CompletableFuture;

import com.tuhuynh.httpserver.core.RequestBinderBase.HttpResponse;

import lombok.Getter;

@Getter
public final class AsyncHelper {
    public static AsyncHelper make() {
        CompletableFuture<HttpResponse> completableFuture = new CompletableFuture<>();
        return new AsyncHelper(completableFuture);
    }

    CompletableFuture<HttpResponse> promise;

    private AsyncHelper(final CompletableFuture<HttpResponse> promise) {
        this.promise = promise;
    }

    public <T> void resolve(final T t) {
        promise.complete(HttpResponse.of(t));
    }

    public CompletableFuture<HttpResponse> submit() {
        return promise;
    }
}
