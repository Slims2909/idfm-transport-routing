package fr.u_paris.gla.project.idfm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVParser {
    private static final String DELIMITER = ",";
    
    public interface CSVLineParser<T> {
        T parse(String[] fields, Map<String, Integer> headerMap);
    }
    
    public static <T> List<T> parseCSV(String filePath, CSVLineParser<T> lineParser) throws IOException {
        List<T> results = new ArrayList<>();
        Map<String, Integer> headerMap = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Read header
            String headerLine = br.readLine();
            if (headerLine != null) {
                String[] headers = headerLine.split(DELIMITER);
                for (int i = 0; i < headers.length; i++) {
                    headerMap.put(headers[i].trim(), i);
                }
            }
            
            // Read data
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(DELIMITER);
                // Skip empty lines
                if (fields.length > 0 && !fields[0].trim().isEmpty()) {
                    T item = lineParser.parse(fields, headerMap);
                    if (item != null) {
                        results.add(item);
                    }
                }
            }
        }
        
        return results;
    }
    
    // Utility method to safely get field value
    public static String getField(String[] fields, Map<String, Integer> headerMap, String fieldName) {
        Integer index = headerMap.get(fieldName);
        if (index != null && index < fields.length) {
            return fields[index].trim();
        }
        return "";
    }
    
    // Utility method to parse double with default value
    public static double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
