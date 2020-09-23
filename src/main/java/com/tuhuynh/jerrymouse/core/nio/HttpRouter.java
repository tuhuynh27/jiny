package com.tuhuynh.jerrymouse.core.nio;

import com.tuhuynh.jerrymouse.core.ParserUtils;
import com.tuhuynh.jerrymouse.core.RequestBinder.NIOHandlerMetadata;
import com.tuhuynh.jerrymouse.core.RequestBinder.BaseHandlerMetadata;
import com.tuhuynh.jerrymouse.core.RequestBinder.RequestHandlerNIO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ArrayList;

@NoArgsConstructor
@Getter
public class HttpRouter {
    private final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> middlewares = new ArrayList<>();
    private final ArrayList<BaseHandlerMetadata<RequestHandlerNIO>> handlers = new ArrayList<>();

    public void addHandler(final ParserUtils.RequestMethod method, final String path,
                           final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(method, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void use(final RequestHandlerNIO... handlers) {
        val newMiddleware = new NIOHandlerMetadata(ParserUtils.RequestMethod.ALL, "/", handlers);
        this.middlewares.add(newMiddleware);
    }

    public void use(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(ParserUtils.RequestMethod.ALL, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void get(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(ParserUtils.RequestMethod.GET, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void post(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(ParserUtils.RequestMethod.POST, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void put(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(ParserUtils.RequestMethod.PUT, path, handlers);
        this.handlers.add(newHandlers);
    }

    public void delete(final String path, final RequestHandlerNIO... handlers) {
        val newHandlers = new NIOHandlerMetadata(ParserUtils.RequestMethod.DELETE, path, handlers);
        this.handlers.add(newHandlers);
    }
}
