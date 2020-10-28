package com.jinyframework.core;

import com.jinyframework.core.AbstractRequestBinder.HandlerBase;
import com.jinyframework.core.AbstractRequestBinder.HandlerMetadata;
import com.jinyframework.core.AbstractRequestBinder.RequestTransformer;
import com.jinyframework.core.utils.ParserUtils.HttpMethod;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Abstract http router.
 *
 * @param <T> the type parameter
 */
@Getter
public abstract class AbstractHttpRouter<T extends HandlerBase> {
    /**
     * The Handlers.
     */
    protected final List<HandlerMetadata<T>> handlers = new ArrayList<>();
    /**
     * The Middlewares.
     */
    protected final List<HandlerMetadata<T>> middlewares = new ArrayList<>();
    /**
     * The Transformer.
     */
    protected RequestTransformer transformer = Object::toString;

    /**
     * The Response headers.
     */
    protected Map<String, String> responseHeaders = new HashMap<>();

    /**
     * Use.
     *
     * @param path   the path
     * @param router the router
     */
    public final void use(@NonNull final String path, @NonNull final AbstractHttpRouter<T> router) {
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

    /**
     * Add handler.
     *
     * @param method   the method
     * @param path     the path
     * @param handlers the handlers
     */
    @SafeVarargs
    public final void addHandler(@NonNull final HttpMethod method, @NonNull final String path,
                                 @NonNull final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(method, path, handlers);
        this.handlers.add(newHandlers);
    }

    /**
     * Use.
     *
     * @param handlers the handlers
     */
    @SafeVarargs
    public final void use(@NonNull final T... handlers) {
        val newMiddleware = new HandlerMetadata<T>(HttpMethod.ALL, "/", handlers);
        this.middlewares.add(newMiddleware);
    }

    /**
     * Use.
     *
     * @param path     the path
     * @param handlers the handlers
     */
    @SafeVarargs
    public final void use(@NonNull final String path, @NonNull final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(HttpMethod.ALL, path, handlers);
        this.handlers.add(newHandlers);
    }

    /**
     * Get.
     *
     * @param path     the path
     * @param handlers the handlers
     */
    @SafeVarargs
    public final void get(@NonNull final String path, @NonNull final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(HttpMethod.GET, path, handlers);
        this.handlers.add(newHandlers);
    }

    /**
     * Post.
     *
     * @param path     the path
     * @param handlers the handlers
     */
    @SafeVarargs
    public final void post(@NonNull final String path, @NonNull final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(HttpMethod.POST, path, handlers);
        this.handlers.add(newHandlers);
    }

    /**
     * Put.
     *
     * @param path     the path
     * @param handlers the handlers
     */
    @SafeVarargs
    public final void put(@NonNull final String path, @NonNull final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(HttpMethod.PUT, path, handlers);
        this.handlers.add(newHandlers);
    }

    /**
     * Patch.
     *
     * @param path     the path
     * @param handlers the handlers
     */
    @SafeVarargs
    public final void patch(@NonNull final String path, @NonNull final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(HttpMethod.PATCH, path, handlers);
        this.handlers.add(newHandlers);
    }

    /**
     * Delete.
     *
     * @param path     the path
     * @param handlers the handlers
     */
    @SafeVarargs
    public final void delete(@NonNull final String path, @NonNull final T... handlers) {
        val newHandlers = new HandlerMetadata<T>(HttpMethod.DELETE, path, handlers);
        this.handlers.add(newHandlers);
    }
}
