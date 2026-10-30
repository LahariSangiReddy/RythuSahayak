package com.rythusahayak.util;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class JdbcFeatureDemo {
    private static final String DEMO_SELECT =
            "SELECT id, label FROM jdbc_feature_demo WHERE id >= ? ORDER BY id";

    private JdbcFeatureDemo() {
    }

    public static void main(String[] args) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            printDatabaseMetadata(connection.getMetaData());
            printScrollableAndResultMetadata(connection);
            printUpdatableSupport(connection);
            printRowSetExamples();
            demonstrateReadOnlyTransactions(connection);
        }
    }

    private static void printDatabaseMetadata(DatabaseMetaData metadata) throws SQLException {
        System.out.printf("Database: %s %s%n", metadata.getDatabaseProductName(),
                metadata.getDatabaseProductVersion());
        System.out.printf("JDBC driver: %s %s%n", metadata.getDriverName(), metadata.getDriverVersion());
        printSupport(metadata, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY,
                "Scrollable read-only ResultSet");
        printSupport(metadata, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE,
                "Scrollable updatable ResultSet");
    }

    private static void printSupport(DatabaseMetaData metadata, int type, int concurrency, String label)
            throws SQLException {
        System.out.printf("%s supported: %s%n", label,
                metadata.supportsResultSetType(type) && metadata.supportsResultSetConcurrency(type, concurrency));
    }

    private static void printScrollableAndResultMetadata(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                DEMO_SELECT, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            statement.setLong(1, 1);
            try (ResultSet results = statement.executeQuery()) {
                ResultSetMetaData metadata = results.getMetaData();
                System.out.printf("ResultSet columns (%d):", metadata.getColumnCount());
                for (int column = 1; column <= metadata.getColumnCount(); column++) {
                    System.out.printf(" %s", metadata.getColumnName(column));
                }
                System.out.println();
                if (results.getType() != ResultSet.TYPE_SCROLL_INSENSITIVE) {
                    System.out.println("Driver returned a forward-only ResultSet; scrolling is unavailable.");
                } else if (results.first()) {
                    System.out.printf("First demo row: %d %s%n", results.getLong("id"), results.getString("label"));
                    if (results.last()) {
                        System.out.printf("Last demo row: %d %s%n", results.getLong("id"), results.getString("label"));
                    }
                } else {
                    System.out.println("No demo rows found. Run database/jdbc-demo.sql to add sample rows.");
                }
            }
        } catch (SQLException exception) {
            throw new SQLException("Scrollable demo query failed; run database/jdbc-demo.sql first.", exception);
        }
    }

    private static void printUpdatableSupport(Connection connection) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        if (!metadata.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE)
                || !metadata.supportsResultSetConcurrency(
                        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            System.out.println("Driver does not support a scrollable updatable ResultSet for this demo.");
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                DEMO_SELECT, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            statement.setLong(1, 1);
            try (ResultSet results = statement.executeQuery()) {
                boolean updatable = results.getConcurrency() == ResultSet.CONCUR_UPDATABLE;
                System.out.printf("Updatable demo ResultSet granted: %s (no rows modified)%n", updatable);
            }
        } catch (SQLException exception) {
            System.out.printf("Driver refused the updatable demo ResultSet: %s%n", exception.getSQLState());
        }
    }

    private static void printRowSetExamples() throws SQLException {
        try (JdbcRowSet connected = RowSetProvider.newFactory().createJdbcRowSet()) {
            connected.setCommand(DEMO_SELECT);
            connected.setLong(1, 1);
            DBConnection.configureJdbcRowSet(connected);
            connected.execute();
            if (connected.next()) {
                System.out.printf("Connected JdbcRowSet first row: %d %s%n",
                        connected.getLong("id"), connected.getString("label"));
            } else {
                System.out.println("Connected JdbcRowSet returned no demo rows.");
            }

            try (CachedRowSet disconnected = RowSetProvider.newFactory().createCachedRowSet();
                 Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(DEMO_SELECT)) {
                statement.setLong(1, 1);
                try (ResultSet results = statement.executeQuery()) {
                    disconnected.populate(results);
                }
                if (disconnected.next()) {
                    System.out.printf("Disconnected CachedRowSet first row: %d %s%n",
                            disconnected.getLong("id"), disconnected.getString("label"));
                } else {
                    System.out.println("Disconnected CachedRowSet returned no demo rows.");
                }
            }
        } catch (SQLException exception) {
            throw new SQLException("RowSet demo failed; run database/jdbc-demo.sql first.", exception);
        }
    }

    private static void demonstrateReadOnlyTransactions(Connection connection) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            runCountQuery(connection);
            connection.commit();
            System.out.println("Read-only transaction committed.");

            runCountQuery(connection);
            connection.rollback();
            System.out.println("Read-only transaction rolled back; no table rows were changed.");
        } catch (SQLException exception) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                exception.addSuppressed(rollbackException);
            }
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private static void runCountQuery(Connection connection) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT COUNT(*) FROM jdbc_feature_demo WHERE id >= ?")) {
            statement.setLong(1, 1);
            try (ResultSet results = statement.executeQuery()) {
                results.next();
                System.out.printf("Demo rows visible in transaction: %d%n", results.getLong(1));
            }
        }
    }
}
