package com.jinyframework.middlewares.cors;

import lombok.val;

import java.util.ArrayList;

public final class Util {
    public static String normalizeHeader(String h) {
        val split = h.split("-");
        val res = new ArrayList<String>();
        for (val word:split) {
            res.add(word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase());
        }
        return String.join("-", res);
    }
}
