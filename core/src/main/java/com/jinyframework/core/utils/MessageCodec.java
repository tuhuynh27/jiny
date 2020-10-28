package com.jinyframework.core.utils;

import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;

/**
 * The type Message codec.
 */
@UtilityClass
public final class MessageCodec {
    /**
     * Encode byte buffer.
     *
     * @param msg the msg
     * @return the byte buffer
     */
    public ByteBuffer encode(final String msg) {
        return ByteBuffer.wrap(msg.getBytes());
    }

    /**
     * Decode string.
     *
     * @param buffer the buffer
     * @return the string
     */
    public String decode(final ByteBuffer buffer) {
        return new String(buffer.array(), buffer.arrayOffset(), buffer.remaining());
    }
}
