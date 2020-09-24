package com.tuhuynh.jerrymouse.core;

import com.tuhuynh.jerrymouse.core.RequestBinderBase.BaseHandlerMetadata;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.RequestHandlerBase;
import com.tuhuynh.jerrymouse.core.utils.ParserUtils.HttpMethod;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Getter
public abstract class HttpRouterBase<T extends RequestHandlerBase> {
    protected final ArrayList<BaseHandlerMetadata<T>> handlers = new ArrayList<>();
    protected final ArrayList<BaseHandlerMetadata<T>> middlewares = new ArrayList<>();

    public final void use(final String path, final HttpRouterBase<T> router) {
        val refactoredMiddlewares = router.getMiddlewares().stream().peek(e -> {
            val refactoredPath = path + e.getPath();
            e.setPath(refactoredPath);
        }).collect(Collectors.toList());

        val refactoredHandlers = router.getHandlers().stream().peek(e -> {
            val refactoredPath = path + e.getPath();
            e.setPath(refactoredPath);
        }).collect(Collectors.toList());

        middlewares.addAll(refactoredMiddlewares);
        handlers.addAll(refactoredHandlers);
    }

    @SafeVarargs
    public final void addHandler(final HttpMethod method, final String path,
                                 final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(method, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void use(final T... handlers) {
        val newMiddleware = new BaseHandlerMetadata<T>(HttpMethod.ALL, "/", handlers);
        this.middlewares.add(newMiddleware);
    }

    @SafeVarargs
    public final void use(final String path, final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(HttpMethod.ALL, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void get(final String path, final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(HttpMethod.GET, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void post(final String path, final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(HttpMethod.POST, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void put(final String path, final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(HttpMethod.PUT, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void patch(final String path, final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(HttpMethod.PATCH, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void delete(final String path, final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(HttpMethod.DELETE, path, handlers);
        this.handlers.add(newHandlers);
    }
}
