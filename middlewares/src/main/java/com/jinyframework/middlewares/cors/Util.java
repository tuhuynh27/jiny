package com.jinyframework.middlewares.cors;

import java.util.ArrayList;

import lombok.NonNull;
import lombok.val;

/**
 * The type Util.
 */
public final class Util {
    /**
     * Normalize header string to uppercase the first letter after hyphen, and lowercase everything else.
     * <p/>
     * e.g: "the-RANDOM-HeadEr" to "The-Random-Header"
     *
     * @param h the header
     * @return the formatted header
     */
    public static String normalizeHeader(@NonNull String h) {
        val split = h.split("-");
        val res = new ArrayList<String>();
        for (val word : split) {
            res.add(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
        }
        return String.join("-", res);
    }
}
