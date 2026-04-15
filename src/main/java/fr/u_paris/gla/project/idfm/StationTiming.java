package fr.u_paris.gla.project.idfm;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import fr.u_paris.gla.project.utils.GPS;

public class StationTiming {
    private final String stationName;
    private final LocalTime arrivalTime;
    private static final double AVERAGE_SPEED_KMH = 30.0; // Vitesse moyenne en km/h
    
    public StationTiming(String stationName, LocalTime arrivalTime) {
        this.stationName = stationName;
        this.arrivalTime = arrivalTime;
    }
    
    public String getStationName() { return stationName; }
    public LocalTime getArrivalTime() { return arrivalTime; }
    
    private static int estimateDuration(double distanceKm) {
        // Convertir la vitesse en km/minute
        double speedKmPerMinute = AVERAGE_SPEED_KMH / 60.0;
        // Calculer la durée en minutes
        return (int) Math.ceil(distanceKm / speedKmPerMinute);
    }
    
    public static List<StationTiming> calculateStationTimings(Schedule schedule, Map<String, List<StopInfo>> stopsByRoute) {
        List<StationTiming> timings = new ArrayList<>();
        List<StopInfo> stops = stopsByRoute.get(schedule.getLineId());
        
        if (stops == null || stops.isEmpty()) {
            return timings;
        }
        
        // Trouver l'index du terminus de départ
        int startIndex = -1;
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).getName().equals(schedule.getStartTerminus())) {
                startIndex = i;
                break;
            }
        }
        
        if (startIndex == -1) {
            System.err.println("Warning: Terminus de départ non trouvé: " + schedule.getStartTerminus());
            return timings;
        }
        
        LocalTime currentTime = schedule.getDepartureTime();
        timings.add(new StationTiming(schedule.getStartTerminus(), currentTime));
        
        // Liste des arrêts à visiter dans l'ordre
        List<Integer> stopOrder = new ArrayList<>();
        stopOrder.add(startIndex);
        
        // Ajouter les bifurcations si elles existent
        List<Integer> junctions = schedule.getJunctions();
        if (!junctions.isEmpty()) {
            stopOrder.addAll(junctions);
        }
        
        // Parcourir les arrêts dans l'ordre
        for (int i = 0; i < stopOrder.size() - 1; i++) {
            int currentIdx = stopOrder.get(i);
            int nextIdx = stopOrder.get(i + 1);
            
            // Calculer le temps de trajet entre ces deux arrêts
            if (currentIdx < nextIdx) {
                // Parcours dans l'ordre croissant
                for (int j = currentIdx; j < nextIdx; j++) {
                    StopInfo current = stops.get(j);
                    StopInfo next = stops.get(j + 1);
                    double distance = GPS.distance(current.getLat(), current.getLon(), 
                                                next.getLat(), next.getLon());
                    int duration = estimateDuration(distance);
                    currentTime = currentTime.plusMinutes(duration);
                    timings.add(new StationTiming(next.getName(), currentTime));
                }
            } else {
                // Parcours dans l'ordre décroissant
                for (int j = currentIdx; j > nextIdx; j--) {
                    StopInfo current = stops.get(j);
                    StopInfo next = stops.get(j - 1);
                    double distance = GPS.distance(current.getLat(), current.getLon(), 
                                                next.getLat(), next.getLon());
                    int duration = estimateDuration(distance);
                    currentTime = currentTime.plusMinutes(duration);
                    timings.add(new StationTiming(next.getName(), currentTime));
                }
            }
        }
        
        return timings;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s", stationName, arrivalTime.toString());
    }
}
