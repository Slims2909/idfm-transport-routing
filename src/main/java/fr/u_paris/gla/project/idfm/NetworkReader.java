package fr.u_paris.gla.project.idfm;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class NetworkReader {
    
    private static double[] parseCoordinates(String coords) {
        // Format attendu: "48,79838,2,60505" -> [48.79838, 2.60505]
        String[] parts = coords.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Format de coordonnées invalide: " + coords);
        }
        
        double lat = Double.parseDouble(parts[0] + "." + parts[1]);
        double lon = Double.parseDouble(parts[2] + "." + parts[3]);
        return new double[] { lat, lon };
    }
    
    public static Map<String, List<StopInfo>> readNetwork(Path networkPath) throws IOException {
        System.out.println("=== LECTURE DU RÉSEAU ===");
        System.out.println("Fichier : " + networkPath);
        
        if (!Files.exists(networkPath)) {
            throw new IOException("Le fichier réseau n'existe pas : " + networkPath);
        }
        
        Map<String, List<StopInfo>> stopsByRoute = new HashMap<>();
        
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(networkPath.toFile()))
                .withCSVParser(new com.opencsv.CSVParserBuilder()
                    .withSeparator(';')
                    .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build())
                .build()) {
            
            // Lire et vérifier l'en-tête
            String[] header = reader.readNext();
            if (header == null || header.length < 8) {  // Modifié pour correspondre au format
                throw new IOException("En-tête du fichier invalide");
            }
            System.out.println("En-tête : " + String.join(";", header));
            
            String[] line;
            int lineNumber = 1;
            int totalStops = 0;
            Set<String> uniqueLines = new HashSet<>();
            
            while ((line = reader.readNext()) != null) {
                lineNumber++;
                try {
                    if (line.length >= 8) {  // Modifié pour correspondre au format
                        String lineId = line[0].trim();
                        String stopName1 = line[2].trim();
                        String coords1 = line[3].trim();
                        
                        double[] coords = parseCoordinates(coords1);
                        StopInfo stop = new StopInfo(stopName1, coords[0], coords[1]);
                        
                        stopsByRoute.computeIfAbsent(lineId, k -> new ArrayList<>())
                                  .add(stop);
                        
                        uniqueLines.add(lineId);
                        totalStops++;
                        
                        if (lineNumber <= 3) {
                            System.out.printf("Exemple ligne %d: %s -> %s (%.6f, %.6f)%n", 
                                lineNumber, lineId, stopName1, coords[0], coords[1]);
                        }
                    } else {
                        System.err.printf("Ligne %d : nombre de colonnes incorrect (%d)%n", 
                            lineNumber, line.length);
                    }
                } catch (Exception e) {
                    System.err.printf("Erreur ligne %d : %s%n", lineNumber, e.getMessage());
                    System.err.println("Contenu : " + String.join(";", line));
                }
            }
            
            System.out.printf("Total lignes lues : %d%n", lineNumber - 1);
            System.out.printf("Total arrêts : %d%n", totalStops);
            System.out.printf("Nombre de lignes uniques : %d%n", uniqueLines.size());
            
            // Afficher quelques statistiques sur les lignes
            for (Map.Entry<String, List<StopInfo>> entry : stopsByRoute.entrySet()) {
                String lineId = entry.getKey();
                List<StopInfo> stops = entry.getValue();
                System.out.printf("Ligne %s : %d arrêts%n", lineId, stops.size());
            }
            
            return stopsByRoute;
            
        } catch (CsvException e) {
            throw new IOException("Erreur de lecture du fichier réseau", e);
        }
    }
}
