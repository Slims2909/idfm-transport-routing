package fr.u_paris.gla.project.idfm;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ScheduleDemo {
    private static final int BATCH_SIZE = 10; // Traiter 10 horaires à la fois
    private static final int MAX_SCHEDULES = 100; // Limite le nombre total d'horaires à traiter
    
    public static void main(String[] args) {
        try {
            System.err.println("\n=== DÉBUT GÉNÉRATION ALL_TIMINGS ===");
            
            // 1. Vérifier si les fichiers existent déjà
            Path networkPath = Paths.get("data/network.csv");
            Path schedulesPath = Paths.get("data/schedules.csv");
            Path outputPath = Paths.get("data/all_timings.csv");
            
            System.err.println("\nVérification des chemins :");
            System.err.println("- Réseau : " + networkPath.toAbsolutePath() + " (existe: " + Files.exists(networkPath) + ")");
            System.err.println("- Horaires : " + schedulesPath.toAbsolutePath() + " (existe: " + Files.exists(schedulesPath) + ")");
            System.err.println("- Sortie : " + outputPath.toAbsolutePath());
            
            if (!Files.exists(networkPath)) {
                throw new IOException("Le fichier network.csv n'existe pas : " + networkPath.toAbsolutePath());
            }
            
            if (!Files.exists(schedulesPath)) {
                throw new IOException("Le fichier schedules.csv n'existe pas : " + schedulesPath.toAbsolutePath());
            }
            
            // 2. Lire les horaires
            System.err.println("\nLecture des horaires...");
            List<Schedule> schedules = ScheduleReader.readSchedules(schedulesPath);
            int totalSchedules = Math.min(schedules.size(), MAX_SCHEDULES);
            System.err.printf("Nombre d'horaires à traiter : %d%n", totalSchedules);
            
            if (schedules.isEmpty()) {
                throw new RuntimeException("Aucun horaire n'a été lu depuis " + schedulesPath);
            }
            
            // 3. Lire le réseau pour obtenir les arrêts par ligne
            System.err.println("\nLecture du réseau...");
            Map<String, List<StopInfo>> stopsByRoute = NetworkReader.readNetwork(networkPath);
            System.err.printf("Nombre de lignes : %d%n", stopsByRoute.size());
            
            if (stopsByRoute.isEmpty()) {
                throw new RuntimeException("Aucune ligne n'a été lue depuis " + networkPath);
            }
            
            // 4. Générer les horaires pour chaque station
            System.err.println("\nGénération des horaires par station...");
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile()))) {
                writer.println("LineID;DepartureTime;StationName;ArrivalTime");
                
                List<Schedule> batch = new ArrayList<>(BATCH_SIZE);
                int processedSchedules = 0;
                
                for (Schedule schedule : schedules) {
                    if (processedSchedules >= MAX_SCHEDULES) {
                        break;
                    }
                    
                    batch.add(schedule);
                    
                    if (batch.size() >= BATCH_SIZE) {
                        System.err.printf("\nTraitement du lot %d-%d...%n", 
                            processedSchedules + 1, processedSchedules + batch.size());
                        processScheduleBatch(batch, stopsByRoute, writer);
                        processedSchedules += batch.size();
                        System.err.printf("Progression : %d/%d horaires traités (%.1f%%)%n", 
                            processedSchedules, totalSchedules, 
                            (processedSchedules * 100.0) / totalSchedules);
                        batch.clear();
                    }
                }
                
                // Traiter le dernier lot
                if (!batch.isEmpty()) {
                    System.err.printf("\nTraitement du dernier lot %d-%d...%n", 
                        processedSchedules + 1, processedSchedules + batch.size());
                    processScheduleBatch(batch, stopsByRoute, writer);
                    processedSchedules += batch.size();
                    System.err.printf("Progression : %d/%d horaires traités (%.1f%%)%n", 
                        processedSchedules, totalSchedules, 
                        (processedSchedules * 100.0) / totalSchedules);
                }
            }
            
            if (!Files.exists(outputPath)) {
                throw new RuntimeException("Le fichier all_timings.csv n'a pas été créé !");
            }
            
            System.err.println("\nTraitement terminé !");
            System.err.println("Les horaires ont été écrits dans : " + outputPath.toAbsolutePath());
            System.err.println("=== FIN GÉNÉRATION ALL_TIMINGS ===\n");
            
        } catch (Exception e) {
            System.err.println("\n!!! ERREUR FATALE !!!");
            System.err.println("Nature de l'erreur : " + e.getClass().getSimpleName());
            System.err.println("Message : " + e.getMessage());
            System.err.println("\nStack trace complet :");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
    
    private static void processScheduleBatch(List<Schedule> batch, 
                                       Map<String, List<StopInfo>> stopsByRoute,
                                       PrintWriter writer) {
        for (Schedule schedule : batch) {
            List<StationTiming> timings = StationTiming.calculateStationTimings(schedule, stopsByRoute);
            
            for (StationTiming timing : timings) {
                writer.printf("%s;%s;%s;%s%n",
                    schedule.getLineId(),
                    schedule.getDepartureTime(),
                    timing.getStationName(),
                    timing.getArrivalTime());
            }
        }
    }
}
