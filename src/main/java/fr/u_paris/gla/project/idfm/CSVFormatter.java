package fr.u_paris.gla.project.idfm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.opencsv.ICSVParser;

import java.io.FileReader;
import java.io.FileWriter;
import fr.u_paris.gla.project.utils.GPS;

public class CSVFormatter {
    // Vitesse moyenne en km/h selon le type de transport
    private static final double BUS_SPEED_KMH = 20.0;      // Vitesse moyenne en ville
    private static final double TRAM_SPEED_KMH = 25.0;     // Vitesse moyenne pour les trams
    private static final double TRAIN_SPEED_KMH = 40.0;    // Vitesse moyenne pour les trains
    private static final double DEFAULT_SPEED_KMH = 25.0;  // Vitesse par défaut
    private static final double STOP_TIME_MINUTES = 0.5;   // Temps d'arrêt à chaque station
    
    public static void formatNetworkData(Path tracesPath, Path stopsPath, Path outputPath) throws IOException {
        try (CSVReader tracesReader = new CSVReaderBuilder(new FileReader(tracesPath.toFile()))
                .withCSVParser(new com.opencsv.CSVParserBuilder()
                    .withSeparator(';')
                    .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build())
                .build();
             CSVReader stopsReader = new CSVReaderBuilder(new FileReader(stopsPath.toFile()))
                .withCSVParser(new com.opencsv.CSVParserBuilder()
                    .withSeparator(';')
                    .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build())
                .build();
             CSVWriter writer = new CSVWriter(new FileWriter(outputPath.toFile()), ';', 
                CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            
            // Lire toutes les données
            List<String[]> tracesRows = tracesReader.readAll();
            List<String[]> stopsRows = stopsReader.readAll();
            
            // Créer une map des arrêts par route_id
            Map<String, List<StopInfo>> stopsByRoute = new HashMap<>();
            for (String[] stop : stopsRows.subList(1, stopsRows.size())) { // Skip header
                if (stop.length < 6) { // Vérifier que nous avons assez de colonnes
                    System.err.println("Warning: Ligne ignorée - pas assez de colonnes: " + String.join(";", stop));
                    continue;
                }
                String routeId = stop[0];
                try {
                    stopsByRoute.computeIfAbsent(routeId, k -> new ArrayList<>())
                        .add(new StopInfo(stop[3], // stop_name
                                        Double.parseDouble(stop[5]), // stop_lat
                                        Double.parseDouble(stop[4]))); // stop_lon
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Ligne ignorée - coordonnées invalides pour route_id " + routeId);
                }
            }
            
            // Écrire l'en-tête
            writer.writeNext(new String[]{"LineID", "Direction", "StopName1", "Coords1", 
                                        "StopName2", "Coords2", "Duration", "Distance"});
            
            // Traiter chaque ligne
            for (String[] trace : tracesRows.subList(1, tracesRows.size())) { // Skip header
                String lineId = trace[1]; // short_name
                List<StopInfo> stops = stopsByRoute.get(trace[0]); // route_id
                
                if (stops != null && stops.size() >= 2) {
                    // Traiter les arrêts consécutifs
                    for (int i = 0; i < stops.size() - 1; i++) {
                        StopInfo current = stops.get(i);
                        StopInfo next = stops.get(i + 1);
                        
                        // Calculer la distance en km
                        double distance = GPS.distance(current.getLat(), current.getLon(), 
                                                     next.getLat(), next.getLon());
                        
                        // Calculer le temps de trajet estimé (en minutes:secondes)
                        String duration = formatDuration(estimateDuration(distance));
                        
                        // Formater les coordonnées
                        String coords1 = String.format("%.5f,%.5f", current.getLat(), current.getLon());
                        String coords2 = String.format("%.5f,%.5f", next.getLat(), next.getLon());
                        
                        // Écrire la ligne
                        writer.writeNext(new String[]{
                            lineId,                    // LineID
                            String.valueOf(i),         // Direction (index as simple direction)
                            current.getName(),              // StopName1
                            coords1,                   // Coords1
                            next.getName(),                 // StopName2
                            coords2,                   // Coords2
                            duration,                  // Duration
                            String.format("%.2f", distance) // Distance
                        });
                    }
                }
            }
            
            System.out.println("Données du réseau formatées avec succès");
            
        } catch (CsvException e) {
            System.err.println("Erreur lors du formatage des données: " + e.getMessage());
            throw new IOException("Erreur lors du formatage des données", e);
        }
    }
    
    public static double estimateDuration(double distanceKm) {
        // Utiliser la vitesse par défaut pour le moment
        // Dans une version future, on pourrait déterminer le type de transport basé sur le lineId
        double speedKmMin = DEFAULT_SPEED_KMH / 60.0;
        
        // Temps de trajet = temps de parcours + temps d'arrêt
        double travelTime = distanceKm / speedKmMin;
        
        // Ajouter le temps d'arrêt
        return travelTime + STOP_TIME_MINUTES;
    }
    
    private static String formatDuration(double minutes) {
        int totalSeconds = (int) (minutes * 60);
        int mins = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%d:%02d", mins, secs);
    }
    
    public static Map<String, List<StopInfo>> readStopsByRoute(Path networkPath) throws IOException {
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(networkPath.toFile()))
                .withCSVParser(new com.opencsv.CSVParserBuilder()
                    .withSeparator(';')
                    .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build())
                .build()) {
            
            List<String[]> rows = reader.readAll();
            Map<String, List<StopInfo>> stopsByRoute = new HashMap<>();
            
            // Skip header
            for (String[] row : rows.subList(1, rows.size())) {
                String lineId = row[0];
                String stopName1 = row[2];
                String[] coords1 = row[3].split(",");
                
                stopsByRoute.computeIfAbsent(lineId, k -> new ArrayList<>())
                    .add(new StopInfo(stopName1, 
                        Double.parseDouble(coords1[0]), 
                        Double.parseDouble(coords1[1])));
                
                // Add the second stop if it's not already in the list
                String stopName2 = row[4];
                String[] coords2 = row[5].split(",");
                List<StopInfo> stops = stopsByRoute.get(lineId);
                
                boolean found = false;
                for (StopInfo stop : stops) {
                    if (stop.getName().equals(stopName2)) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    stops.add(new StopInfo(stopName2,
                        Double.parseDouble(coords2[0]),
                        Double.parseDouble(coords2[1])));
                }
            }
            
            return stopsByRoute;
            
        } catch (CsvException e) {
            throw new IOException("Error reading network file", e);
        }
    }
}
