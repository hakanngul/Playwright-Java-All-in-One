package com.starlettech.utils;

import com.starlettech.config.TestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database utilities for test data setup and cleanup
 */
public class DatabaseUtils {
    private static final Logger logger = LogManager.getLogger(DatabaseUtils.class);
    private static final TestConfig testConfig = TestConfig.getInstance();
    
    // Connection pool
    private static final Map<String, Connection> connectionPool = new ConcurrentHashMap<>();
    
    // Database configuration
    private static final String DEFAULT_DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String DEFAULT_DB_USER = "sa";
    private static final String DEFAULT_DB_PASSWORD = "";
    private static final String DEFAULT_DB_DRIVER = "org.h2.Driver";

    /**
     * Get database connection
     */
    public static Connection getConnection() throws SQLException {
        return getConnection("default");
    }

    /**
     * Get database connection by name
     */
    public static Connection getConnection(String connectionName) throws SQLException {
        Connection connection = connectionPool.get(connectionName);
        
        if (connection == null || connection.isClosed()) {
            connection = createConnection(connectionName);
            connectionPool.put(connectionName, connection);
            logger.debug("Created new database connection: {}", connectionName);
        }
        
        return connection;
    }

    /**
     * Create new database connection
     */
    private static Connection createConnection(String connectionName) throws SQLException {
        try {
            String dbUrl = getDbProperty(connectionName, "url", DEFAULT_DB_URL);
            String dbUser = getDbProperty(connectionName, "user", DEFAULT_DB_USER);
            String dbPassword = getDbProperty(connectionName, "password", DEFAULT_DB_PASSWORD);
            String dbDriver = getDbProperty(connectionName, "driver", DEFAULT_DB_DRIVER);
            
            // Load database driver
            Class.forName(dbDriver);
            
            Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            connection.setAutoCommit(false); // Use transactions
            
            logger.info("Database connection established: {} -> {}", connectionName, dbUrl);
            return connection;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }

    /**
     * Execute SQL query and return results
     */
    public static List<Map<String, Object>> executeQuery(String sql, Object... parameters) throws SQLException {
        return executeQuery("default", sql, parameters);
    }

    /**
     * Execute SQL query with specific connection
     */
    public static List<Map<String, Object>> executeQuery(String connectionName, String sql, Object... parameters) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection connection = getConnection(connectionName);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            // Set parameters
            setParameters(statement, parameters);
            
            logger.debug("Executing query: {} with parameters: {}", sql, Arrays.toString(parameters));
            
            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = resultSet.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
            }
            
            logger.debug("Query returned {} rows", results.size());
        }
        
        return results;
    }

    /**
     * Execute SQL update/insert/delete
     */
    public static int executeUpdate(String sql, Object... parameters) throws SQLException {
        return executeUpdate("default", sql, parameters);
    }

    /**
     * Execute SQL update with specific connection
     */
    public static int executeUpdate(String connectionName, String sql, Object... parameters) throws SQLException {
        try (Connection connection = getConnection(connectionName);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            // Set parameters
            setParameters(statement, parameters);
            
            logger.debug("Executing update: {} with parameters: {}", sql, Arrays.toString(parameters));
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            logger.debug("Update affected {} rows", rowsAffected);
            return rowsAffected;
        }
    }

    /**
     * Execute batch updates
     */
    public static int[] executeBatch(String sql, List<Object[]> parametersList) throws SQLException {
        return executeBatch("default", sql, parametersList);
    }

    /**
     * Execute batch updates with specific connection
     */
    public static int[] executeBatch(String connectionName, String sql, List<Object[]> parametersList) throws SQLException {
        try (Connection connection = getConnection(connectionName);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            logger.debug("Executing batch: {} with {} parameter sets", sql, parametersList.size());
            
            for (Object[] parameters : parametersList) {
                setParameters(statement, parameters);
                statement.addBatch();
            }
            
            int[] results = statement.executeBatch();
            connection.commit();
            
            logger.debug("Batch execution completed");
            return results;
        }
    }

    /**
     * Create test data table
     */
    public static void createTestDataTable(String tableName, Map<String, String> columns) throws SQLException {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        
        List<String> columnDefinitions = new ArrayList<>();
        for (Map.Entry<String, String> column : columns.entrySet()) {
            columnDefinitions.add(column.getKey() + " " + column.getValue());
        }
        
        sql.append(String.join(", ", columnDefinitions));
        sql.append(")");
        
        executeUpdate(sql.toString());
        logger.info("Test data table created: {}", tableName);
    }

    /**
     * Insert test data
     */
    public static void insertTestData(String tableName, Map<String, Object> data) throws SQLException {
        List<String> columns = new ArrayList<>(data.keySet());
        List<Object> values = new ArrayList<>(data.values());
        
        String sql = "INSERT INTO " + tableName + " (" + String.join(", ", columns) + ") VALUES (" +
                     String.join(", ", Collections.nCopies(columns.size(), "?")) + ")";
        
        executeUpdate(sql, values.toArray());
        logger.debug("Test data inserted into {}: {}", tableName, data);
    }

    /**
     * Insert multiple test data records
     */
    public static void insertTestData(String tableName, List<Map<String, Object>> dataList) throws SQLException {
        if (dataList.isEmpty()) return;
        
        Map<String, Object> firstRecord = dataList.get(0);
        List<String> columns = new ArrayList<>(firstRecord.keySet());
        
        String sql = "INSERT INTO " + tableName + " (" + String.join(", ", columns) + ") VALUES (" +
                     String.join(", ", Collections.nCopies(columns.size(), "?")) + ")";
        
        List<Object[]> parametersList = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            Object[] parameters = columns.stream().map(data::get).toArray();
            parametersList.add(parameters);
        }
        
        executeBatch(sql, parametersList);
        logger.info("Inserted {} test data records into {}", dataList.size(), tableName);
    }

    /**
     * Clean up test data
     */
    public static void cleanupTestData(String tableName) throws SQLException {
        executeUpdate("DELETE FROM " + tableName);
        logger.info("Test data cleaned up from table: {}", tableName);
    }

    /**
     * Clean up test data with condition
     */
    public static void cleanupTestData(String tableName, String whereClause, Object... parameters) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE " + whereClause;
        int deletedRows = executeUpdate(sql, parameters);
        logger.info("Cleaned up {} test data records from {}", deletedRows, tableName);
    }

    /**
     * Drop test data table
     */
    public static void dropTestDataTable(String tableName) throws SQLException {
        executeUpdate("DROP TABLE IF EXISTS " + tableName);
        logger.info("Test data table dropped: {}", tableName);
    }

    /**
     * Check if table exists
     */
    public static boolean tableExists(String tableName) throws SQLException {
        try (Connection connection = getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
                return resultSet.next();
            }
        }
    }

    /**
     * Get table row count
     */
    public static int getRowCount(String tableName) throws SQLException {
        List<Map<String, Object>> results = executeQuery("SELECT COUNT(*) as count FROM " + tableName);
        return results.isEmpty() ? 0 : ((Number) results.get(0).get("count")).intValue();
    }

    /**
     * Backup table data
     */
    public static List<Map<String, Object>> backupTableData(String tableName) throws SQLException {
        List<Map<String, Object>> backup = executeQuery("SELECT * FROM " + tableName);
        logger.info("Backed up {} records from table: {}", backup.size(), tableName);
        return backup;
    }

    /**
     * Restore table data
     */
    public static void restoreTableData(String tableName, List<Map<String, Object>> backupData) throws SQLException {
        // Clean existing data
        cleanupTestData(tableName);
        
        // Restore backup data
        if (!backupData.isEmpty()) {
            insertTestData(tableName, backupData);
            logger.info("Restored {} records to table: {}", backupData.size(), tableName);
        }
    }

    /**
     * Set prepared statement parameters
     */
    private static void setParameters(PreparedStatement statement, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            Object parameter = parameters[i];
            if (parameter == null) {
                statement.setNull(i + 1, Types.NULL);
            } else if (parameter instanceof String) {
                statement.setString(i + 1, (String) parameter);
            } else if (parameter instanceof Integer) {
                statement.setInt(i + 1, (Integer) parameter);
            } else if (parameter instanceof Long) {
                statement.setLong(i + 1, (Long) parameter);
            } else if (parameter instanceof Double) {
                statement.setDouble(i + 1, (Double) parameter);
            } else if (parameter instanceof Boolean) {
                statement.setBoolean(i + 1, (Boolean) parameter);
            } else if (parameter instanceof java.sql.Date) {
                statement.setDate(i + 1, (java.sql.Date) parameter);
            } else if (parameter instanceof Timestamp) {
                statement.setTimestamp(i + 1, (Timestamp) parameter);
            } else {
                statement.setObject(i + 1, parameter);
            }
        }
    }

    /**
     * Get database property
     */
    private static String getDbProperty(String connectionName, String property, String defaultValue) {
        String key = "db." + connectionName + "." + property;
        return testConfig.getProperty(key) != null ? testConfig.getProperty(key) : 
               System.getProperty(key, defaultValue);
    }

    /**
     * Close all database connections
     */
    public static void closeAllConnections() {
        for (Map.Entry<String, Connection> entry : connectionPool.entrySet()) {
            try {
                Connection connection = entry.getValue();
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    logger.debug("Closed database connection: {}", entry.getKey());
                }
            } catch (SQLException e) {
                logger.error("Error closing database connection {}: {}", entry.getKey(), e.getMessage());
            }
        }
        connectionPool.clear();
        logger.info("All database connections closed");
    }

    /**
     * Initialize test database with sample schema
     */
    public static void initializeTestDatabase() throws SQLException {
        // Create sample users table
        Map<String, String> userColumns = new HashMap<>();
        userColumns.put("id", "INTEGER PRIMARY KEY AUTO_INCREMENT");
        userColumns.put("username", "VARCHAR(50) NOT NULL");
        userColumns.put("email", "VARCHAR(100) NOT NULL");
        userColumns.put("password", "VARCHAR(100) NOT NULL");
        userColumns.put("created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        
        createTestDataTable("test_users", userColumns);
        
        // Create sample products table
        Map<String, String> productColumns = new HashMap<>();
        productColumns.put("id", "INTEGER PRIMARY KEY AUTO_INCREMENT");
        productColumns.put("name", "VARCHAR(100) NOT NULL");
        productColumns.put("price", "DECIMAL(10,2) NOT NULL");
        productColumns.put("category", "VARCHAR(50)");
        productColumns.put("in_stock", "BOOLEAN DEFAULT TRUE");
        
        createTestDataTable("test_products", productColumns);
        
        logger.info("Test database initialized with sample schema");
    }
}
