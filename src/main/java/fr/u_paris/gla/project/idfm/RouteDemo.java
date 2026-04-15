package fr.u_paris.gla.project.idfm;

import fr.u_paris.gla.project.idfm.graph.Node;
import fr.u_paris.gla.project.idfm.graph.PathFinder;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.*;

public class RouteDemo {
    private static final String NETWORK_FILE = "data/network.csv";
    private static final String SCHEDULE_FILE = "data/schedules.csv";
    
    public static void main(String[] args) {
        try {
            // Charger le réseau
            NetworkReader networkReader = new NetworkReader();
            Map<String, List<StopInfo>> lines = networkReader.readNetwork(Paths.get(NETWORK_FILE));
            
            // Créer le PathFinder
            PathFinder finder = new PathFinder();
            
            // Charger les horaires
            ScheduleReader scheduleReader = new ScheduleReader();
            List<Schedule> schedules = scheduleReader.readSchedules(Paths.get(SCHEDULE_FILE));
            
            // Ajouter les lignes au PathFinder
            for (Map.Entry<String, List<StopInfo>> entry : lines.entrySet()) {
                String lineId = entry.getKey();
                List<StopInfo> stops = entry.getValue();
                
                // Calculer les durées et ajouter la ligne
                List<Integer> durations = calculateDurations(stops, schedules, lineId);
                finder.addLine(lineId, stops, durations);
            }
            
            // Interface utilisateur simple
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\nStation de départ (ou 'q' pour quitter) : ");
                String from = scanner.nextLine().trim();
                if (from.equalsIgnoreCase("q")) break;
                
                System.out.print("Station d'arrivée : ");
                String to = scanner.nextLine().trim();
                
                List<Node> path = finder.findShortestPath(from, to);
                System.out.println("\nItinéraire trouvé :");
                System.out.println(finder.formatPath(path));
            }
            
            scanner.close();
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des données : " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static List<Integer> calculateDurations(List<StopInfo> stops, List<Schedule> schedules, String lineId) {
        List<Integer> durations = new ArrayList<>();
        
        for (int i = 0; i < stops.size() - 1; i++) {
            String station1 = stops.get(i).getName();
            String station2 = stops.get(i + 1).getName();
            
            List<Integer> tripDurations = new ArrayList<>();
            for (Schedule schedule : schedules) {
                if (schedule.getLineId().equals(lineId)) {
                    int duration = getDuration(schedule, station1, station2);
                    if (duration > 0) {
                        tripDurations.add(duration);
                    }
                }
            }
            
            int avgDuration = tripDurations.isEmpty() ? 
                estimateDuration(stops.get(i), stops.get(i + 1)) : 
                (int) tripDurations.stream().mapToInt(Integer::intValue).average().getAsDouble();
            
            durations.add(avgDuration);
        }
        
        return durations;
    }
    
    private static int getDuration(Schedule schedule, String station1, String station2) {
        LocalTime time1 = schedule.getDepartureTime();
        if (time1 == null) {
            return -1;
        }
        
        // Pour l'instant, on estime la durée car on n'a pas accès aux horaires détaillés
        return 5; // 5 minutes par défaut entre stations
    }
    
    private static int parseTime(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
    
    private static int estimateDuration(StopInfo stop1, StopInfo stop2) {
        double distance = calculateDistance(
            stop1.getLat(), stop1.getLon(),
            stop2.getLat(), stop2.getLon()
        );
        return (int) Math.ceil(distance / 30.0 * 60);
    }
    
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
