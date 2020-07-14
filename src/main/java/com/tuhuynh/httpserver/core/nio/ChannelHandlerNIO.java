package com.tuhuynh.httpserver.core.nio;

public interface ChannelHandlerNIO {
    void read() throws Exception;

    void write() throws Exception;
}
