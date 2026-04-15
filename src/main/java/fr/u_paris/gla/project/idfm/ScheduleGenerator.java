package fr.u_paris.gla.project.idfm;

import java.io.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;

public class ScheduleGenerator {
    private static final int SCHEDULE_INTERVAL_MINUTES = 30;

    public static void generateSchedules(Path networkPath, Path outputPath) throws IOException {
        System.out.println("=== DÉBUT GÉNÉRATION HORAIRES ===");
        System.out.println("Fichier réseau : " + networkPath);
        System.out.println("Fichier sortie : " + outputPath);
        
        // Lire le réseau pour obtenir les lignes et leurs terminus
        Map<String, String> lineTerminus = new HashMap<>();
        
        try (BufferedReader reader = Files.newBufferedReader(networkPath)) {
            String header = reader.readLine(); // Skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    String lineId = parts[0].trim();
                    String terminus = parts[2].trim(); // StopName1
                    
                    if (!lineTerminus.containsKey(lineId)) {
                        lineTerminus.put(lineId, terminus);
                    }
                }
            }
        }
        
        // Générer les horaires
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile()))) {
            writer.println("LineID;Junctions;StartTerminus;DepartureTime");
            
            LocalTime startTime = LocalTime.of(7, 0); // 7h00
            LocalTime endTime = LocalTime.of(9, 0);  // 9h00
            
            for (Map.Entry<String, String> entry : lineTerminus.entrySet()) {
                String lineId = entry.getKey();
                String terminus = entry.getValue();
                
                LocalTime currentTime = startTime;
                while (!currentTime.isAfter(endTime)) {
                    writer.printf("%s;[2,1,3];%s;%d:%02d",
                        lineId,
                        terminus,
                        currentTime.getHour(),
                        currentTime.getMinute());
                    
                    currentTime = currentTime.plusMinutes(SCHEDULE_INTERVAL_MINUTES);
                }
            }
        }
        
        System.out.println("Fichier généré : " + outputPath);
        System.out.println("=== FIN GÉNÉRATION HORAIRES ===");
    }
}
