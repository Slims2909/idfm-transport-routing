package fr.u_paris.gla.project.idfm;

import java.util.*;

/**
 * Extension de TransportGraph avec des fonctionnalités spécifiques à IDFM
 */
public class GrapheTransport extends TransportGraph {
    
    private Map<String, Station> stationsById = new HashMap<>();
    
    public void loadFromCSVProvider(CSVStreamProvider provider) {
        while (provider.hasNext()) {
            String[] data = provider.next();
            
            // Créer les stations si elles n'existent pas
            String startId = data[NetworkFormat.START_STOP_ID_INDEX];
            String endId = data[NetworkFormat.END_STOP_ID_INDEX];
            
            Station start = new Station(startId, 
                data[NetworkFormat.START_STOP_NAME_INDEX],
                Double.parseDouble(data[NetworkFormat.START_STOP_LAT_INDEX]),
                Double.parseDouble(data[NetworkFormat.START_STOP_LON_INDEX]));
                
            Station end = new Station(endId,
                data[NetworkFormat.END_STOP_NAME_INDEX],
                Double.parseDouble(data[NetworkFormat.END_STOP_LAT_INDEX]),
                Double.parseDouble(data[NetworkFormat.END_STOP_LON_INDEX]));
            
            // Ajouter le segment
            double distance = Double.parseDouble(data[NetworkFormat.DISTANCE_INDEX]);
            double duration = parseTime(data[NetworkFormat.DURATION_INDEX]);
            String lineId = data[NetworkFormat.LINE_INDEX];
            
            Segment segment = new Segment(start, end, duration, distance, lineId);
            addSegment(segment);
        }
    }
    
    private static double parseTime(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60.0 + minutes;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    @Override
    public void addStation(Station station) {
        super.addStation(station);
        stationsById.put(station.getId(), station);
    }
    
    public Station getStation(String id) {
        return stationsById.get(id);
    }
    
    public Set<Station> getNeighborStations(Station station) {
        Set<Station> neighbors = new HashSet<>();
        for (Segment segment : getSegments()) {
            if (segment.getStation1().equals(station)) {
                neighbors.add(segment.getStation2());
            } else if (segment.getStation2().equals(station)) {
                neighbors.add(segment.getStation1());
            }
        }
        return neighbors;
    }
    
    public Set<Station> getNeighborStationsOnLine(Station station, String lineId) {
        Set<Station> neighbors = new HashSet<>();
        for (Segment segment : getSegments()) {
            if (segment.getLineId().equals(lineId)) {
                if (segment.getStation1().equals(station)) {
                    neighbors.add(segment.getStation2());
                } else if (segment.getStation2().equals(station)) {
                    neighbors.add(segment.getStation1());
                }
            }
        }
        return neighbors;
    }
    
    public Set<Segment> getNeighbors(Station station) {
        return getAdjacentSegments(station);
    }
    
    public boolean isConnected() {
        if (getStations().isEmpty()) return true;
        
        Set<Station> visited = new HashSet<>();
        Queue<Station> queue = new LinkedList<>();
        
        Station start = getStations().iterator().next();
        queue.add(start);
        visited.add(start);
        
        while (!queue.isEmpty()) {
            Station current = queue.poll();
            for (Station neighbor : getNeighborStations(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        return visited.size() == getStations().size();
    }
    
    public Set<Station> getIsolatedStations() {
        Set<Station> isolated = new HashSet<>(getStations());
        for (Segment segment : getSegments()) {
            isolated.remove(segment.getStation1());
            isolated.remove(segment.getStation2());
        }
        return isolated;
    }
    
    public void cleanGraph() {
        // Supprime les segments en double en gardant le plus rapide
        Map<String, Segment> bestSegments = new HashMap<>();
        
        System.out.println("Nombre de segments avant nettoyage : " + segments.size());
        
        // Parcourir tous les segments et garder le plus rapide pour chaque paire de stations et ligne
        for (Segment segment : new ArrayList<>(segments)) {
            // Créer une clé unique pour chaque paire de stations et ligne
            String key = makeSegmentKey(segment);
            
            System.out.println("Traitement segment : " + segment + " avec clé : " + key);
            
            Segment existing = bestSegments.get(key);
            if (existing == null || segment.getDuration() < existing.getDuration()) {
                System.out.println("  -> Ajout/Remplacement segment : " + segment);
                bestSegments.put(key, segment);
            } else {
                System.out.println("  -> Segment ignoré car plus lent que : " + existing);
            }
        }
        
        // Vider le graphe et réajouter les meilleurs segments
        segments.clear();
        for (Set<Segment> adjacentSegments : adjacencyList.values()) {
            adjacentSegments.clear();
        }
        
        for (Segment segment : bestSegments.values()) {
            Station station1 = segment.getStation1();
            Station station2 = segment.getStation2();
            adjacencyList.get(station1).add(segment);
            adjacencyList.get(station2).add(segment);
            segments.add(segment);
        }
        
        System.out.println("Nombre de segments après nettoyage : " + segments.size());
    }
    
    private String makeSegmentKey(Segment segment) {
        String station1Id = segment.getStation1().getId();
        String station2Id = segment.getStation2().getId();
        // Toujours mettre l'ID le plus petit en premier pour avoir une clé cohérente
        if (station1Id.compareTo(station2Id) <= 0) {
            return station1Id + "-" + station2Id + "-" + segment.getLineId();
        } else {
            return station2Id + "-" + station1Id + "-" + segment.getLineId();
        }
    }
    
    public void displayGraphStats() {
        System.out.println("Statistiques du graphe :");
        System.out.println("Nombre de stations : " + getStations().size());
        System.out.println("Nombre de segments : " + getSegments().size());
        System.out.println("Stations isolées : " + getIsolatedStations().size());
        System.out.println("Graphe connexe : " + isConnected());
    }
    
    /**
     * Vérifie la validité du graphe
     */
    public ValidationResult validate() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();
        
        // Vérifier que toutes les stations ont des coordonnées valides
        for (Station station : getStations()) {
            if (station.getLatitude() < -90 || station.getLatitude() > 90 ||
                station.getLongitude() < -180 || station.getLongitude() > 180) {
                isValid = false;
                errors.append("Coordonnées invalides pour la station: ")
                      .append(station).append("\n");
            }
        }
        
        return new ValidationResult(isValid, errors.toString());
    }
    
    /**
     * Résultat de la validation du graphe
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String errors;
        
        public ValidationResult(boolean isValid, String errors) {
            this.isValid = isValid;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public String getErrors() {
            return errors;
        }
    }
    
    @Override
    public void addSegment(Segment segment) {
        Station station1 = segment.getStation1();
        Station station2 = segment.getStation2();
        
        // Ajoute les stations si elles n'existent pas
        addStation(station1);
        addStation(station2);
        
        // Ajoute toujours le segment, même s'il existe déjà
        // Le nettoyage des doublons sera fait par cleanGraph()
        adjacencyList.get(station1).add(segment);
        adjacencyList.get(station2).add(segment);
        segments.add(segment);
    }
    
    public Set<Station> getStations() {
        return stations;
    }
}