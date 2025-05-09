package fr.u_paris.gla.project.idfm;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransportDataLoader {
    private final Map<String, Stop> stops = new HashMap<>();
    private final Map<String, Ligne> lignes = new HashMap<>();
    
    public void loadStops(String filePath) throws IOException {
        List<Stop> loadedStops = CSVParser.parseCSV(filePath, (fields, headerMap) -> {
            String id = CSVParser.getField(fields, headerMap, "stop_id");
            String name = CSVParser.getField(fields, headerMap, "stop_name");
            double lat = CSVParser.parseDouble(CSVParser.getField(fields, headerMap, "stop_lat"), 0.0);
            double lon = CSVParser.parseDouble(CSVParser.getField(fields, headerMap, "stop_lon"), 0.0);
            String type = CSVParser.getField(fields, headerMap, "location_type");
            
            Stop stop = new Stop(id, name, lat, lon, type);
            stops.put(id, stop);
            return stop;
        });
        
        System.out.printf("Loaded %d stops%n", loadedStops.size());
    }
    
    public void loadLignes(String filePath) throws IOException {
        List<Ligne> loadedLignes = CSVParser.parseCSV(filePath, (fields, headerMap) -> {
            String id = CSVParser.getField(fields, headerMap, "route_id");
            String name = CSVParser.getField(fields, headerMap, "route_long_name");
            String type = CSVParser.getField(fields, headerMap, "route_type");
            String color = CSVParser.getField(fields, headerMap, "route_color");
            
            Ligne ligne = new Ligne(id, name, type, color);
            lignes.put(id, ligne);
            return ligne;
        });
        
        System.out.printf("Loaded %d lignes%n", loadedLignes.size());
    }
    
    public void loadSegments(String filePath) throws IOException {
        List<Segment> segments = CSVParser.parseCSV(filePath, (fields, headerMap) -> {
            String ligneId = CSVParser.getField(fields, headerMap, "route_id");
            String fromStopId = CSVParser.getField(fields, headerMap, "from_stop_id");
            String toStopId = CSVParser.getField(fields, headerMap, "to_stop_id");
            double duration = CSVParser.parseDouble(CSVParser.getField(fields, headerMap, "duration"), 0.0);
            double distance = CSVParser.parseDouble(CSVParser.getField(fields, headerMap, "distance"), 0.0);
            
            Stop fromStop = stops.get(fromStopId);
            Stop toStop = stops.get(toStopId);
            Ligne ligne = lignes.get(ligneId);
            
            if (fromStop != null && toStop != null && ligne != null) {
                Segment segment = new Segment(fromStop, toStop, (int)duration, distance);
                ligne.addSegment(segment);
                return segment;
            }
            
            return null;
        });
        
        System.out.printf("Loaded %d segments%n", segments.size());
    }
    
    // Getters
    public Map<String, Stop> getStops() { return new HashMap<>(stops); }
    public Map<String, Ligne> getLignes() { return new HashMap<>(lignes); }
}
