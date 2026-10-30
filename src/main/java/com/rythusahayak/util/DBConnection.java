package com.rythusahayak.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.rowset.JdbcRowSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static final String CONFIG_PROPERTY = "rythusahayak.db.config";
    private static final String CONFIG_ENVIRONMENT = "RYTHUSAHAYAK_DB_CONFIG";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        LoadedProperties configuration = loadProperties();
        String url = requiredProperty(configuration.properties(), "jdbc.url");
        String username = requiredProperty(configuration.properties(), "jdbc.username");
        String password = requiredProperty(configuration.properties(), "jdbc.password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL Connector/J is missing from the application.", exception);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE,
                    "Database connection failed using config file {0} (exception {1}, SQLState {2}, error {3}).",
                    new Object[]{configuration.path(), exception.getClass().getName(),
                            exception.getSQLState(), exception.getErrorCode()});
            throw exception;
        }
    }

    public static void configureJdbcRowSet(JdbcRowSet rowSet) throws SQLException {
        LoadedProperties configuration = loadProperties();
        rowSet.setUrl(requiredProperty(configuration.properties(), "jdbc.url"));
        rowSet.setUsername(requiredProperty(configuration.properties(), "jdbc.username"));
        rowSet.setPassword(requiredProperty(configuration.properties(), "jdbc.password"));
    }

    private static LoadedProperties loadProperties() throws SQLException {
        Path path = resolveConfigPath();
        if (!Files.isRegularFile(path)) {
            throw new SQLException("Database configuration is missing. Create " + path
                    + " from db.properties.example or set " + CONFIG_ENVIRONMENT + ".");
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        } catch (IOException exception) {
            throw new SQLException("Unable to read database configuration at " + path + ".", exception);
        }
        return new LoadedProperties(path, properties);
    }

    static Path resolveConfigPath() {
        String configuredPath = System.getProperty(CONFIG_PROPERTY);
        if (configuredPath == null || configuredPath.isBlank()) {
            configuredPath = System.getenv(CONFIG_ENVIRONMENT);
        }
        return configuredPath == null || configuredPath.isBlank()
                ? Path.of(System.getProperty("user.home"), ".rythusahayak", "db.properties")
                : Path.of(configuredPath).toAbsolutePath().normalize();
    }

    private static String requiredProperty(Properties properties, String name) throws SQLException {
        String value = properties.getProperty(name);
        if (value == null || value.isBlank() || value.equals("CHANGE_ME")) {
            throw new SQLException("Required database setting is missing or still a placeholder: " + name);
        }
        return value.trim();
    }

    private record LoadedProperties(Path path, Properties properties) {
    }
}
