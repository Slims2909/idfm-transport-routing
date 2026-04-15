package fr.u_paris.gla.project.idfm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVParser {
    private static final String DELIMITER = ",";
    private static final String CHARSET = "UTF-8";
    
    public interface CSVLineParser<T> {
        T parse(String[] fields, Map<String, Integer> headerMap);
    }
    
    public static <T> List<T> parseCSV(String filePath, CSVLineParser<T> lineParser) throws IOException {
        List<T> results = new ArrayList<>();
        Map<String, Integer> headerMap = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath, Charset.forName(CHARSET)))) {
            // Read header
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IOException("Empty CSV file: " + filePath);
            }
            
            String[] headers = headerLine.split(DELIMITER);
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim();
                if (!header.isEmpty()) {
                    headerMap.put(header, i);
                }
            }
            
            if (headerMap.isEmpty()) {
                throw new IOException("No valid headers found in CSV file: " + filePath);
            }
            
            // Read data
            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    String[] fields = line.split(DELIMITER);
                    if (fields.length > 0) {
                        T item = lineParser.parse(fields, headerMap);
                        if (item != null) {
                            results.add(item);
                        }
                    }
                } catch (Exception e) {
                    System.err.printf("Error parsing line %d in %s: %s%n", lineNumber, filePath, e.getMessage());
                }
            }
        }
        
        return results;
    }
    
    // Utility method to safely get field value
    public static String getField(String[] fields, Map<String, Integer> headerMap, String fieldName) {
        Integer index = headerMap.get(fieldName);
        if (index == null) {
            throw new IllegalArgumentException("Column not found: " + fieldName);
        }
        if (index >= fields.length) {
            throw new IllegalArgumentException("Invalid field index for column: " + fieldName);
        }
        return fields[index].trim();
    }
    
    // Utility method to parse double with default value
    public static double parseDouble(String value, double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            System.err.printf("Invalid number format: %s, using default: %f%n", value, defaultValue);
            return defaultValue;
        }
    }
}
