package com.jinyframework.core.utils;

import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;

@UtilityClass
public final class MessageCodec {
    public ByteBuffer encode(final String msg) {
        return ByteBuffer.wrap(msg.getBytes());
    }

    public String decode(final ByteBuffer buffer) {
        return new String(buffer.array(), buffer.arrayOffset(), buffer.remaining());
    }
}
