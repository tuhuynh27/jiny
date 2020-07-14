package com.tuhuynh.httpserver.core;

public interface ChannelHandlerNIO {
    void read() throws Exception;

    void write() throws Exception;
}
