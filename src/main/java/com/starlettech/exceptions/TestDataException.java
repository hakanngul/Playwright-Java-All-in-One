package com.starlettech.exceptions;

/**
 * Exception thrown for test data-related errors
 */
public class TestDataException extends FrameworkException {
    
    private final String dataSource;
    private final String dataKey;
    
    public TestDataException(String message) {
        super(message, ErrorType.DATA, "FW_DATA_001");
        this.dataSource = null;
        this.dataKey = null;
    }
    
    public TestDataException(String message, String dataSource) {
        super(message, ErrorType.DATA, "FW_DATA_002");
        this.dataSource = dataSource;
        this.dataKey = null;
    }
    
    public TestDataException(String message, String dataSource, String dataKey) {
        super(message, ErrorType.DATA, "FW_DATA_003");
        this.dataSource = dataSource;
        this.dataKey = dataKey;
    }
    
    public TestDataException(String message, Throwable cause) {
        super(message, cause, ErrorType.DATA, "FW_DATA_001");
        this.dataSource = null;
        this.dataKey = null;
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public String getDataKey() {
        return dataKey;
    }
    
    // Specific test data error methods
    public static TestDataException fileNotFound(String fileName) {
        return new TestDataException("Test data file not found: " + fileName, fileName);
    }
    
    public static TestDataException dataNotFound(String dataKey, String dataSource) {
        return new TestDataException(
            String.format("Test data not found. Key: '%s', Source: '%s'", dataKey, dataSource),
            dataSource, dataKey);
    }
    
    public static TestDataException invalidDataFormat(String fileName, Throwable cause) {
        return new TestDataException("Invalid test data format in file: " + fileName, cause);
    }
    
    public static TestDataException jsonParseError(String fileName, Throwable cause) {
        return new TestDataException("Failed to parse JSON test data: " + fileName, cause);
    }
    
    public static TestDataException emptyDataSet(String dataSource) {
        return new TestDataException("Empty data set in source: " + dataSource, dataSource);
    }
    
    public static TestDataException userDataNotFound(String userType) {
        return new TestDataException("User data not found for type: " + userType, "users.json", userType);
    }
    
    public static TestDataException payloadNotFound(String payloadName) {
        return new TestDataException("API payload not found: " + payloadName, "api-payloads.json", payloadName);
    }
    
    @Override
    public String toString() {
        String baseString = super.toString();
        if (dataSource != null) {
            baseString += String.format(" [Source: %s]", dataSource);
        }
        if (dataKey != null) {
            baseString += String.format(" [Key: %s]", dataKey);
        }
        return baseString;
    }
}