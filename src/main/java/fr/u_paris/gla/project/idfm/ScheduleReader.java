package fr.u_paris.gla.project.idfm;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class ScheduleReader {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    
    public static List<Schedule> readSchedules(Path schedulePath) throws IOException {
        System.out.println("=== LECTURE DES HORAIRES ===");
        System.out.println("Fichier : " + schedulePath);
        
        if (!Files.exists(schedulePath)) {
            throw new IOException("Le fichier d'horaires n'existe pas : " + schedulePath);
        }
        
        List<Schedule> schedules = new ArrayList<>();
        
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(schedulePath.toFile()))
                .withCSVParser(new com.opencsv.CSVParserBuilder()
                    .withSeparator(';')
                    .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build())
                .build()) {
            
            // Lire et vérifier l'en-tête
            String[] header = reader.readNext();
            if (header == null || header.length < 4) {
                throw new IOException("En-tête du fichier invalide");
            }
            System.out.println("En-tête : " + String.join(";", header));
            
            String[] line;
            int lineNumber = 1;
            while ((line = reader.readNext()) != null) {
                lineNumber++;
                try {
                    if (line.length >= 4) {
                        String lineId = line[0].trim();
                        String junctionsStr = line[1].trim();
                        String startTerminus = line[2].trim();
                        String departureTimeStr = line[3].trim();
                        
                        if (departureTimeStr.endsWith(";")) {
                            departureTimeStr = departureTimeStr.substring(0, departureTimeStr.length() - 1);
                        }
                        
                        Schedule schedule = new Schedule(
                            lineId,
                            parseJunctions(junctionsStr),
                            startTerminus,
                            LocalTime.parse(departureTimeStr, TIME_FORMATTER)
                        );
                        schedules.add(schedule);
                    } else {
                        System.err.printf("Ligne %d : nombre de colonnes incorrect (%d)%n", lineNumber, line.length);
                    }
                } catch (Exception e) {
                    System.err.printf("Erreur ligne %d : %s%n", lineNumber, e.getMessage());
                    System.err.println("Contenu : " + String.join(";", line));
                }
            }
            
            System.out.printf("Nombre d'horaires lus : %d%n", schedules.size());
            if (!schedules.isEmpty()) {
                Schedule first = schedules.get(0);
                System.out.println("Premier horaire : " + first);
                Schedule last = schedules.get(schedules.size() - 1);
                System.out.println("Dernier horaire : " + last);
            }
            
            return schedules;
            
        } catch (CsvException e) {
            throw new IOException("Erreur de lecture du fichier CSV", e);
        }
    }
    
    private static List<Integer> parseJunctions(String junctionsStr) {
        // Enlever les crochets et les espaces
        String cleaned = junctionsStr.replaceAll("[\\[\\]\\s]", "");
        String[] parts = cleaned.split(",");
        List<Integer> junctions = new ArrayList<>();
        
        for (String part : parts) {
            try {
                junctions.add(Integer.parseInt(part));
            } catch (NumberFormatException e) {
                System.err.println("Warning: Impossible de parser la jonction: " + part);
            }
        }
        
        return junctions;
    }
}
