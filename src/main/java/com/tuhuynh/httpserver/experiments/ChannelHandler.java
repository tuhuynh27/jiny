package com.tuhuynh.httpserver.experiments;

public interface ChannelHandler {
    void read() throws Exception;

    void write() throws Exception;
}
