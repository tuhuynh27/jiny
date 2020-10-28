package com.jinyframework.middlewares.cors;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("middlewares.cors.Util")
class UtilTest {
    @Test
    @DisplayName("Normalize header")
    void normalizeHeader() {
        val in = "access-CONTROL-Allow-mEthOd";
        val out = "Access-Control-Allow-Method";
        val res = Util.normalizeHeader(in);
        Assertions.assertEquals(out, res);
    }
}