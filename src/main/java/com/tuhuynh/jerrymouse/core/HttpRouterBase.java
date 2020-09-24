package com.tuhuynh.jerrymouse.core;

import com.tuhuynh.jerrymouse.core.RequestBinder.BaseHandlerMetadata;
import com.tuhuynh.jerrymouse.core.RequestBinder.RequestHandlerBase;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Getter
public abstract class HttpRouterBase<T extends RequestHandlerBase> {
    public final ArrayList<BaseHandlerMetadata<T>> handlers = new ArrayList<>();
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
    public final void addHandler(final ParserUtils.RequestMethod method, final String path,
                                 final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(method, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void use(final T... handlers) {
        val newMiddleware = new BaseHandlerMetadata<T>(ParserUtils.RequestMethod.ALL, "/", handlers);
        this.middlewares.add(newMiddleware);
    }

    @SafeVarargs
    public final void use(final String path, final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(ParserUtils.RequestMethod.ALL, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void get(final String path, final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(ParserUtils.RequestMethod.GET, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void post(final String path, final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(ParserUtils.RequestMethod.POST, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void put(final String path, final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(ParserUtils.RequestMethod.PUT, path, handlers);
        this.handlers.add(newHandlers);
    }

    @SafeVarargs
    public final void delete(final String path, final T... handlers) {
        val newHandlers = new BaseHandlerMetadata<T>(ParserUtils.RequestMethod.DELETE, path, handlers);
        this.handlers.add(newHandlers);
    }
}
