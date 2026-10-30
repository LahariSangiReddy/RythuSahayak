package com.rythusahayak.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {
    @Test
    void hashesAndVerifiesPasswordsWithoutAcceptingWrongOrMalformedHashes() {
        String password = "A farmer's secure password 2026";
        String hash = PasswordUtil.hash(password);

        assertTrue(PasswordUtil.verify(password, hash));
        assertFalse(PasswordUtil.verify("a different password", hash));
        assertFalse(PasswordUtil.verify(password, "not-a-bcrypt-hash"));
        assertFalse(PasswordUtil.verify(null, hash));
    }

    @Test
    void rejectsPasswordsOutsideBcryptLengthLimits() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash("short"));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash("x".repeat(73)));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(" ".repeat(12)));
    }
}
