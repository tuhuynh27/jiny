package com.jinyframework.core;

import com.jinyframework.core.RequestBinderBase.HandlerBase;
import com.jinyframework.core.RequestBinderBase.HandlerMetadata;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Getter
public abstract class HttpRouterBase<T extends HandlerBase> {
    protected final ArrayList<HandlerMetadata<T>> handlers = new ArrayList<>();
    protected final ArrayList<HandlerMetadata<T>> middlewares = new ArrayList<>();

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
        val newHandlers = new HandlerMetadata<T>(method, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void use(final T... handlers) {
        val newMiddleware = new HandlerMetadata<T>(HttpMethod.ALL, "/", handlers);
        this.middlewares.add(newMiddleware);
    }

    @SafeVarargs
    public final void use(final String path, final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(HttpMethod.ALL, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void get(final String path, final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(HttpMethod.GET, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void post(final String path, final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(HttpMethod.POST, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void put(final String path, final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(HttpMethod.PUT, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void patch(final String path, final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(HttpMethod.PATCH, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void delete(final String path, final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(HttpMethod.DELETE, path, handlers);
        this.handlers.add(newHandlers);
    }
}
