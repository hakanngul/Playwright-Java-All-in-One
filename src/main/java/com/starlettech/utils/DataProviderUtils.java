package com.starlettech.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.starlettech.config.TestConfig;

/**
 * Data Provider utilities for TestNG data providers
 */
public class DataProviderUtils {
    private static final Logger logger = LogManager.getLogger(DataProviderUtils.class);
    private static final TestConfig testConfig = TestConfig.getInstance();
    private static final TestDataReader testDataReader = TestDataReader.getInstance();

    /**
     * Read data from Excel file
     */
    @SuppressWarnings("resource")
    public static Object[][] readExcelData(String fileName, String sheetName) {
        List<Map<String, String>> dataList = new ArrayList<>();
        
        try {
            String filePath = testConfig.getTestDataPath() + "/" + fileName;
            try (FileInputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheet(sheetName);
                
                if (sheet == null) {
                    logger.error("Sheet '{}' not found in file '{}'", sheetName, fileName);
                    return new Object[0][0];
                }

                // Get header row
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    logger.error("Header row not found in sheet '{}'", sheetName);
                    return new Object[0][0];
                }
                
                List<String> headers = new ArrayList<>();
                for (Cell cell : headerRow) {
                    headers.add(getCellValueAsString(cell));
                }
                
                // Read data rows
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    Map<String, String> rowData = new HashMap<>();
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        String cellValue = cell != null ? getCellValueAsString(cell) : "";
                        rowData.put(headers.get(j), cellValue);
                    }
                    dataList.add(rowData);
                }
                
            }
            
            logger.info("Read {} rows from Excel file: {}", dataList.size(), fileName);
            
        } catch (IOException e) {
            logger.error("Error reading Excel file {}: {}", fileName, e.getMessage());
            return new Object[0][0];
        }
        
        // Convert to Object[][]
        Object[][] data = new Object[dataList.size()][1];
        for (int i = 0; i < dataList.size(); i++) {
            data[i][0] = dataList.get(i);
        }
        
        return data;
    }

    /**
     * Read data from CSV file
     */
    public static Object[][] readCsvData(String fileName) {
        List<Map<String, String>> dataList = new ArrayList<>();
        
        try {
            String filePath = testConfig.getTestDataPath() + "/" + fileName;
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            
            String line;
            String[] headers = null;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                
                if (isFirstLine) {
                    headers = values;
                    isFirstLine = false;
                    continue;
                }
                
                if (headers != null) {
                    Map<String, String> rowData = new HashMap<>();
                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        rowData.put(headers[i].trim(), values[i].trim());
                    }
                    dataList.add(rowData);
                }
            }
            
            br.close();
            logger.info("Read {} rows from CSV file: {}", dataList.size(), fileName);
            
        } catch (IOException e) {
            logger.error("Error reading CSV file {}: {}", fileName, e.getMessage());
            return new Object[0][0];
        }
        
        // Convert to Object[][]
        Object[][] data = new Object[dataList.size()][1];
        for (int i = 0; i < dataList.size(); i++) {
            data[i][0] = dataList.get(i);
        }
        
        return data;
    }

    /**
     * Read data from JSON file
     */
    public static Object[][] readJsonData(String fileName, String arrayPath) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        try {
            JsonNode jsonData = testDataReader.readJsonData(fileName);
            if (jsonData == null) {
                logger.error("Failed to read JSON file: {}", fileName);
                return new Object[0][0];
            }
            
            JsonNode arrayNode = getJsonNodeByPath(jsonData, arrayPath);
            if (arrayNode == null || !arrayNode.isArray()) {
                logger.error("Array not found at path '{}' in file '{}'", arrayPath, fileName);
                return new Object[0][0];
            }
            
            for (JsonNode item : arrayNode) {
                Map<String, Object> rowData = new HashMap<>();
                item.fields().forEachRemaining(entry -> {
                    rowData.put(entry.getKey(), getJsonValue(entry.getValue()));
                });
                dataList.add(rowData);
            }
            
            logger.info("Read {} rows from JSON file: {}", dataList.size(), fileName);
            
        } catch (Exception e) {
            logger.error("Error reading JSON file {}: {}", fileName, e.getMessage());
            return new Object[0][0];
        }
        
        // Convert to Object[][]
        Object[][] data = new Object[dataList.size()][1];
        for (int i = 0; i < dataList.size(); i++) {
            data[i][0] = dataList.get(i);
        }
        
        return data;
    }

    /**
     * Create data provider from multiple sources
     */
    public static Object[][] combineDataSources(Object[][]... dataSources) {
        List<Object[]> combinedData = new ArrayList<>();
        
        for (Object[][] dataSource : dataSources) {
            Collections.addAll(combinedData, dataSource);
        }
        
        return combinedData.toArray(Object[][]::new);
    }

    /**
     * Filter data based on criteria
     */
    public static Object[][] filterData(Object[][] data, String key, String value) {
        List<Object[]> filteredData = new ArrayList<>();
        
        for (Object[] row : data) {
            if (row.length > 0 && row[0] instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rowData = (Map<String, Object>) row[0];
                if (value.equals(String.valueOf(rowData.get(key)))) {
                    filteredData.add(row);
                }
            }
        }
        
        logger.info("Filtered data: {} rows match criteria {}={}", filteredData.size(), key, value);
        return filteredData.toArray(Object[][]::new);
    }

    /**
     * Create parameterized test name
     */
    public static String createTestName(String baseName, Map<String, Object> testData) {
        StringBuilder testName = new StringBuilder(baseName);
        
        if (testData.containsKey("testName")) {
            testName.append("_").append(testData.get("testName"));
        } else if (testData.containsKey("scenario")) {
            testName.append("_").append(testData.get("scenario"));
        } else if (testData.containsKey("id")) {
            testName.append("_").append(testData.get("id"));
        }
        
        return testName.toString().replaceAll("[^a-zA-Z0-9_]", "_");
    }

    // ========== Helper Methods ==========

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private static JsonNode getJsonNodeByPath(JsonNode root, String path) {
        if (path == null || path.isEmpty()) {
            return root;
        }
        
        String[] pathParts = path.split("\\.");
        JsonNode current = root;
        
        for (String part : pathParts) {
            if (current.has(part)) {
                current = current.get(part);
            } else {
                return null;
            }
        }
        
        return current;
    }

    private static Object getJsonValue(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        } else if (node.isNumber()) {
            return node.asDouble();
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isNull()) {
            return null;
        } else {
            return node.toString();
        }
    }
}
