package com.jinyframework.core.helpers;

import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import lombok.Getter;
import lombok.val;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The type Async helper.
 */
@Getter
public final class AsyncHelper {
    /**
     * The Promise.
     */
    CompletableFuture<HttpResponse> promise;

    private AsyncHelper(final CompletableFuture<HttpResponse> promise) {
        this.promise = promise;
    }

    /**
     * Make async helper.
     *
     * @return the async helper
     */
    public static AsyncHelper make() {
        val completableFuture = new CompletableFuture<HttpResponse>();
        return new AsyncHelper(completableFuture);
    }

    /**
     * Resolve.
     *
     * @param <T> the type parameter
     * @param t   the t
     */
    public <T> void resolve(final T t) {
        promise.complete(HttpResponse.of(t));
    }

    /**
     * Reject.
     *
     * @param <T> the type parameter
     * @param t   the t
     */
    public <T extends Error> void reject(final T t) {
        promise.completeExceptionally(t);
    }

    /**
     * Then completable future.
     *
     * @param action the action
     * @return the completable future
     */
    public CompletableFuture<Void> then(Consumer<? super HttpResponse> action) {
        return promise.thenAccept(action);
    }

    /**
     * Submit completable future.
     *
     * @return the completable future
     */
    public CompletableFuture<HttpResponse> submit() {
        return promise;
    }
}
