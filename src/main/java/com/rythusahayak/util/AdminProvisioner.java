package com.rythusahayak.util;

import com.rythusahayak.dao.UserDAO;
import com.rythusahayak.model.User;

import java.io.Console;
import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Pattern;

public final class AdminProvisioner {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9][0-9]{9}$");

    private AdminProvisioner() {
    }

    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            System.err.println("Run this provisioner from a terminal that supports hidden password input.");
            System.exit(1);
        }

        try {
            UserDAO users = new UserDAO();
            if (users.hasAdministrator()) {
                System.err.println("An administrator already exists. Provisioning is first-admin-only.");
                System.exit(1);
            }

            String fullName = console.readLine("Administrator name: ").trim();
            String email = console.readLine("Administrator email: ").trim().toLowerCase(Locale.ROOT);
            String mobile = console.readLine("Mobile (optional, 10 digits): ").trim();
            char[] password = console.readPassword("New password (12–72 characters): ");
            char[] confirmation = console.readPassword("Confirm password: ");
            try {
                String passwordValue = new String(password);
                if (fullName.length() < 2 || fullName.length() > 80
                        || !EMAIL_PATTERN.matcher(email).matches() || email.length() > 254
                        || (!mobile.isEmpty() && !MOBILE_PATTERN.matcher(mobile).matches())
                        || passwordValue.length() < 12 || passwordValue.length() > 72
                        || !java.security.MessageDigest.isEqual(
                                passwordValue.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                                new String(confirmation).getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
                    throw new IllegalArgumentException("Invalid input or passwords did not match.");
                }
                users.createUser(fullName, email, mobile.isEmpty() ? null : mobile,
                        PasswordUtil.hash(passwordValue), User.Role.ADMIN);
                console.printf("Administrator account created for %s.%n", email);
            } finally {
                java.util.Arrays.fill(password, '\0');
                java.util.Arrays.fill(confirmation, '\0');
            }
        } catch (SQLException exception) {
            System.err.printf("Administrator provisioning failed (SQLState %s, error %d).%n",
                    exception.getSQLState(), exception.getErrorCode());
            System.exit(1);
        } catch (IllegalArgumentException exception) {
            System.err.println("Administrator provisioning failed: " + exception.getMessage());
            System.exit(1);
        }
    }
}
