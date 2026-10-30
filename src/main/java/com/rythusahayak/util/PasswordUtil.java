package com.rythusahayak.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private static final int WORK_FACTOR = 12;

    private PasswordUtil() {
    }

    public static String hash(String password) {
        if (password == null || password.isBlank() || password.length() < 12 || password.length() > 72) {
            throw new IllegalArgumentException("Password must be non-blank and between 12 and 72 characters.");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean verify(String password, String passwordHash) {
        if (password == null || passwordHash == null || password.length() > 72
                || !passwordHash.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$")) {
            return false;
        }
        return BCrypt.checkpw(password, passwordHash);
    }
}
