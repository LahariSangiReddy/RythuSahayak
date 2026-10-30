package com.rythusahayak.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DBConnectionTest {
    private static final String CONFIG_PROPERTY = "rythusahayak.db.config";

    @Test
    void systemPropertySelectsTheDatabaseConfigPath(@TempDir Path tempDir) {
        String previousValue = System.getProperty(CONFIG_PROPERTY);
        Path expectedPath = tempDir.resolve("local-db.properties").toAbsolutePath().normalize();
        try {
            System.setProperty(CONFIG_PROPERTY, expectedPath.toString());

            assertEquals(expectedPath, DBConnection.resolveConfigPath());
        } finally {
            if (previousValue == null) {
                System.clearProperty(CONFIG_PROPERTY);
            } else {
                System.setProperty(CONFIG_PROPERTY, previousValue);
            }
        }
    }
}
