package com.jinyframework.core.nio;

import com.jinyframework.core.RequestBinderBase.HttpResponse;
import lombok.Getter;
import lombok.val;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Getter
public final class AsyncHelper {
    CompletableFuture<HttpResponse> promise;

    private AsyncHelper(final CompletableFuture<HttpResponse> promise) {
        this.promise = promise;
    }

    public static AsyncHelper make() {
        val completableFuture = new CompletableFuture<HttpResponse>();
        return new AsyncHelper(completableFuture);
    }

    public <T> void resolve(final T t) {
        promise.complete(HttpResponse.of(t));
    }

    public <T extends Error> void reject(final T t) {
        promise.completeExceptionally(t);
    }

    public CompletableFuture<Void> then(Consumer<? super HttpResponse> action) {
        return promise.thenAccept(action);
    }

    public CompletableFuture<HttpResponse> submit() {
        return promise;
    }
}
