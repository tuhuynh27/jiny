package com.tuhuynh.httpserver;

public interface ChannelHandlerNIO {
    void read() throws Exception;

    void write() throws Exception;
}
