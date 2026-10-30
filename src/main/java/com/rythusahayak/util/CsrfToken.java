package com.rythusahayak.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class CsrfToken {
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfToken() {
    }

    public static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
