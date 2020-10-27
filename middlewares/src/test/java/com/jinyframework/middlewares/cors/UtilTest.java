package com.jinyframework.middlewares.cors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("middlewares.cors.Util")
class UtilTest {
    @Test
    @DisplayName("Normalize header")
    void normalizeHeader() {
        String in = "access-CONTROL-Allow-mEthOd";
        String out = "Access-Control-Allow-Method";
        String res = Util.normalizeHeader(in);
        Assertions.assertEquals(out,res);
    }
}