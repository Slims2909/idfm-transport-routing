package fr.u_paris.gla.project.idfm;

import java.io.*;
import java.util.*;

public class TransportDataLoader {
    private final Map<String, Stop> stops;
    private final TransportGraph graph;
    private final Map<String, Ligne> lignes;
    
    public TransportDataLoader(TransportGraph graph) {
        this.stops = new HashMap<>();
        this.graph = graph;
        this.lignes = new HashMap<>();
    }
    
    public void loadStops(String filePath) throws IOException {
        List<Stop> loadedStops = CSVParser.parseCSV(filePath, (fields, headerMap) -> {
            try {
                String id = CSVParser.getField(fields, headerMap, "stop_id");
                String name = CSVParser.getField(fields, headerMap, "stop_name");
                double lat = CSVParser.parseDouble(CSVParser.getField(fields, headerMap, "stop_lat"), 0.0);
                double lon = CSVParser.parseDouble(CSVParser.getField(fields, headerMap, "stop_lon"), 0.0);
                String type = CSVParser.getField(fields, headerMap, "location_type");
                
                // Vérifier les valeurs obligatoires
                if (id.isEmpty() || name.isEmpty()) {
                    System.err.println("Missing required fields (id or name) for stop");
                    return null;
                }
                
                // Vérifier les coordonnées
                if (lat == 0.0 || lon == 0.0) {
                    System.err.println("Invalid coordinates for stop: " + id);
                    return null;
                }
                
                Stop stop = new Stop(id, name, lat, lon, type);
                addStop(stop);
                return stop;
            } catch (Exception e) {
                System.err.println("Error parsing stop: " + e.getMessage());
                return null;
            }
        });
        
        System.out.printf("Loaded %d stops%n", loadedStops.size());
    }
    
    public void loadLignes(String filePath) throws IOException {
        List<Ligne> loadedLignes = CSVParser.parseCSV(filePath, (fields, headerMap) -> {
            try {
                String id = CSVParser.getField(fields, headerMap, "route_id");
                String name = CSVParser.getField(fields, headerMap, "route_long_name");
                String type = CSVParser.getField(fields, headerMap, "route_type");
                String color = CSVParser.getField(fields, headerMap, "route_color");
                
                // Vérifier les valeurs obligatoires
                if (id.isEmpty() || name.isEmpty()) {
                    System.err.println("Missing required fields (id or name) for ligne");
                    return null;
                }
                
                Ligne ligne = new Ligne(id, name, type, color);
                lignes.put(id, ligne);
                return ligne;
            } catch (Exception e) {
                System.err.println("Error parsing ligne: " + e.getMessage());
                return null;
            }
        });
        
        System.out.printf("Loaded %d lignes%n", loadedLignes.size());
    }
    
    public void loadSegments(String filePath) throws IOException {
        List<Segment> segments = CSVParser.parseCSV(filePath, (fields, headerMap) -> {
            try {
                String ligneId = CSVParser.getField(fields, headerMap, "route_id");
                String fromStopId = CSVParser.getField(fields, headerMap, "from_stop_id");
                String toStopId = CSVParser.getField(fields, headerMap, "to_stop_id");
                double duration = CSVParser.parseDouble(CSVParser.getField(fields, headerMap, "duration"), 0.0);
                double distance = CSVParser.parseDouble(CSVParser.getField(fields, headerMap, "distance"), 0.0);
                
                // Vérifier les valeurs obligatoires
                if (ligneId.isEmpty() || fromStopId.isEmpty() || toStopId.isEmpty()) {
                    System.err.println("Missing required fields for segment");
                    return null;
                }
                
                // Vérifier les valeurs numériques
                if (duration <= 0.0 || distance <= 0.0) {
                    System.err.println("Invalid duration or distance for segment");
                    return null;
                }
                
                addSegment(fromStopId, toStopId, duration, distance, ligneId);
                return new Segment(stops.get(fromStopId), stops.get(toStopId), duration, distance, ligneId);
            } catch (Exception e) {
                System.err.println("Error parsing segment: " + e.getMessage());
                return null;
            }
        });
        
        System.out.printf("Loaded %d segments%n", segments.size());
    }
    
    public void addStop(Stop stop) {
        stops.put(stop.getId(), stop);
    }
    
    public void addSegment(String startId, String endId, double duration, double distance, String lineId) {
        Stop start = stops.get(startId);
        Stop end = stops.get(endId);
        
        if (start == null || end == null) {
            System.err.println("Stop not found: " + (start == null ? startId : endId));
            return;
        }
        
        Segment segment = new Segment(start, end, duration, distance, lineId);
        graph.addSegment(segment);
        
        // Ajouter le segment à la ligne
        Ligne ligne = lignes.get(lineId);
        if (ligne != null) {
            ligne.addSegment(segment);
        }
    }
    
    public TransportGraph getGraph() {
        return graph;
    }
    
    public Map<String, Stop> getStops() {
        return Collections.unmodifiableMap(stops);
    }
    
    public Map<String, Ligne> getLignes() {
        return Collections.unmodifiableMap(lignes);
    }
}
