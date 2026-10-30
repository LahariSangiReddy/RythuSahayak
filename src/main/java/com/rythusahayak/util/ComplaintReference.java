package com.rythusahayak.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.HexFormat;
import java.util.regex.Pattern;

public final class ComplaintReference {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern FORMAT = Pattern.compile("^RYT-[0-9]{8}-[A-F0-9]{16}$");

    private ComplaintReference() {
    }

    public static String generate() {
        byte[] suffix = new byte[8];
        RANDOM.nextBytes(suffix);
        return "RYT-" + LocalDate.now(ZoneOffset.UTC).format(DATE_FORMAT) + "-"
                + HexFormat.of().withUpperCase().formatHex(suffix);
    }

    public static boolean isValid(String reference) {
        if (reference == null || !FORMAT.matcher(reference).matches()) {
            return false;
        }
        try {
            LocalDate.parse(reference.substring(4, 12), DATE_FORMAT);
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }
}
