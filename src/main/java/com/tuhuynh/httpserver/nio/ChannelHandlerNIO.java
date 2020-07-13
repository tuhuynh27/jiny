package com.tuhuynh.httpserver.nio;

public interface ChannelHandlerNIO {
    void read() throws Exception;

    void write() throws Exception;
}
