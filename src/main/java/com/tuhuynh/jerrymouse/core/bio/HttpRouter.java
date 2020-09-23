package com.tuhuynh.jerrymouse.core.bio;

import com.tuhuynh.jerrymouse.core.ParserUtils;
import com.tuhuynh.jerrymouse.core.RequestBinder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ArrayList;

@NoArgsConstructor
@Getter
public final class HttpRouter {
    private final ArrayList<RequestBinder.BaseHandlerMetadata<RequestBinder.RequestHandlerBIO>> middlewares = new ArrayList<>();
    private final ArrayList<RequestBinder.BaseHandlerMetadata<RequestBinder.RequestHandlerBIO>> handlers = new ArrayList<>();

    public void addHandler(final ParserUtils.RequestMethod method, final String path, final RequestBinder.RequestHandlerBIO... handlers) {
        val newHandlers = new RequestBinder.BIOHandlerMetadata(method, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void use(final RequestBinder.RequestHandlerBIO... handlers) {
        val newMiddlewares = new RequestBinder.BIOHandlerMetadata(ParserUtils.RequestMethod.ALL, "/", handlers);
        this.middlewares.add(newMiddlewares);
    }

    public void use(final String path, final RequestBinder.RequestHandlerBIO... handlers) {
        this.addHandler(ParserUtils.RequestMethod.ALL, path, handlers);
    }

    public void get(final String path, final RequestBinder.RequestHandlerBIO... handlers) {
        this.addHandler(ParserUtils.RequestMethod.GET, path, handlers);
    }

    public void post(final String path, final RequestBinder.RequestHandlerBIO... handlers) {
        this.addHandler(ParserUtils.RequestMethod.POST, path, handlers);
    }

    public void put(final String path, final RequestBinder.RequestHandlerBIO... handlers) {
        this.addHandler(ParserUtils.RequestMethod.PUT, path, handlers);
    }

    public void delete(final String path, final RequestBinder.RequestHandlerBIO... handlers) {
        this.addHandler(ParserUtils.RequestMethod.DELETE, path, handlers);
    }
}
