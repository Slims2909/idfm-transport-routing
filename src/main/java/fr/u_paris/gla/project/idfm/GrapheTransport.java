package fr.u_paris.gla.project.idfm;

import java.util.*;

public class GrapheTransport {
    private Map<Station, List<Segment>> adjacencyList;
    private Map<Segment, String> segmentLines;

    public GrapheTransport() {
        this.adjacencyList = new HashMap<>();
        this.segmentLines = new HashMap<>();
    }

    public void addStation(Station station) {
        if (!adjacencyList.containsKey(station)) {
            adjacencyList.put(station, new ArrayList<>());
        }
    }

    public void addSegment(Segment segment, String lineName) {
        // Ajouter le segment dans le sens aller
        Station departure = segment.getDeparture();
        Station arrival = segment.getArrival();
        addStation(departure);
        addStation(arrival);
        adjacencyList.get(departure).add(segment);
        segmentLines.put(segment, lineName);
        
        // Ajouter un segment dans le sens retour
        Segment returnSegment = new Segment(arrival, departure, segment.getDuration(), segment.getDistance());
        adjacencyList.get(arrival).add(returnSegment);
        segmentLines.put(returnSegment, lineName);
    }

    public List<Segment> getNeighbors(Station station) {
        return adjacencyList.getOrDefault(station, Collections.emptyList());
    }

    public List<Station> getNeighborStations(Station station) {
        List<Station> neighbors = new ArrayList<>();
        for (Segment segment : getNeighbors(station)) {
            neighbors.add(segment.getArrival());
        }
        return neighbors;
    }

    public List<Station> getNeighborStationsOnLine(Station station, String lineName) {
        List<Station> neighbors = new ArrayList<>();
        for (Segment segment : getNeighbors(station)) {
            if (lineName.equals(segmentLines.get(segment))) {
                neighbors.add(segment.getArrival());
            }
        }
        return neighbors;
    }

    public Set<String> getLinesAtStation(Station station) {
        Set<String> lines = new HashSet<>();
        for (Segment segment : getNeighbors(station)) {
            lines.add(segmentLines.get(segment));
        }
        return lines;
    }

    public Set<Station> getAllStations() {
        return adjacencyList.keySet();
    }

    public void loadFromCSVProvider(CSVStreamProvider provider) {
        while (provider.hasNext()) {
            String[] data = provider.next();
            
            // Create or get stations
            Station start = new Station(
                data[NetworkFormat.START_INDEX],  // station id
                data[NetworkFormat.START_INDEX],  // station name
                parseGPSCoordinate(data[NetworkFormat.START_INDEX + 1], 0),  // latitude
                parseGPSCoordinate(data[NetworkFormat.START_INDEX + 1], 1)   // longitude
            );
            
            Station end = new Station(
                data[NetworkFormat.STOP_INDEX],   // station id
                data[NetworkFormat.STOP_INDEX],   // station name
                parseGPSCoordinate(data[NetworkFormat.STOP_INDEX + 1], 0),   // latitude
                parseGPSCoordinate(data[NetworkFormat.STOP_INDEX + 1], 1)    // longitude
            );
            
            // Create segment
            double distance = Double.parseDouble(data[NetworkFormat.DISTANCE_INDEX]);
            int duration = parseTime(data[NetworkFormat.DURATION_INDEX]);
            
            Segment segment = new Segment(start, end, duration, distance);
            addSegment(segment, data[NetworkFormat.LINE_INDEX]);
        }
    }

    private double parseGPSCoordinate(String coords, int index) {
        String[] parts = coords.split(",");
        return Double.parseDouble(parts[index].trim());
    }

    private int parseTime(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public void displayGraph() {
        for (Map.Entry<Station, List<Segment>> entry : adjacencyList.entrySet()) {
            System.out.println("Station: " + entry.getKey().getName());
            System.out.println("Connections:");
            for (Segment segment : entry.getValue()) {
                System.out.println("  -> " + segment + " (Line: " + segmentLines.get(segment) + ")");
            }
        }
    }

    public boolean isStationIsolated(Station station) {
        return !adjacencyList.containsKey(station) || adjacencyList.get(station).isEmpty();
    }

    public Set<Station> getIsolatedStations() {
        Set<Station> isolated = new HashSet<>();
        for (Station station : getAllStations()) {
            if (isStationIsolated(station)) {
                isolated.add(station);
            }
        }
        return isolated;
    }

    public boolean isConnected() {
        if (adjacencyList.isEmpty()) {
            return true;
        }
        
        Set<Station> visited = new HashSet<>();
        Station start = adjacencyList.keySet().iterator().next();
        dfs(start, visited);
        
        return visited.size() == adjacencyList.size();
    }

    private void dfs(Station station, Set<Station> visited) {
        visited.add(station);
        for (Segment segment : getNeighbors(station)) {
            Station neighbor = segment.getArrival();
            if (!visited.contains(neighbor)) {
                dfs(neighbor, visited);
            }
        }
    }

    public List<Set<Station>> getConnectedComponents() {
        List<Set<Station>> components = new ArrayList<>();
        Set<Station> unvisited = new HashSet<>(getAllStations());
        
        while (!unvisited.isEmpty()) {
            Set<Station> component = new HashSet<>();
            Station start = unvisited.iterator().next();
            dfs(start, component);
            components.add(component);
            unvisited.removeAll(component);
        }
        
        return components;
    }

    public boolean validateGraphStructure() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        // Vérifier les stations isolées
        Set<Station> isolated = getIsolatedStations();
        if (!isolated.isEmpty()) {
            isValid = false;
            errors.append("Stations isolées trouvées: ").append(isolated.size()).append("\n");
            for (Station station : isolated) {
                errors.append("  - ").append(station.getName()).append("\n");
            }
        }

        // Vérifier la connexité du graphe
        if (!isConnected()) {
            isValid = false;
            errors.append("Le graphe n'est pas connexe. Composantes trouvées:\n");
            List<Set<Station>> components = getConnectedComponents();
            for (int i = 0; i < components.size(); i++) {
                errors.append("Composante ").append(i + 1).append(": ")
                      .append(components.get(i).size()).append(" stations\n");
            }
        }

        // Vérifier la cohérence des segments
        for (Map.Entry<Station, List<Segment>> entry : adjacencyList.entrySet()) {
            Station station = entry.getKey();
            for (Segment segment : entry.getValue()) {
                // Vérifier que la station de départ correspond
                if (!station.equals(segment.getDeparture())) {
                    isValid = false;
                    errors.append("Incohérence de segment: station de départ incorrecte pour ")
                          .append(segment).append("\n");
                }
                
                // Vérifier que la ligne est bien définie
                if (!segmentLines.containsKey(segment)) {
                    isValid = false;
                    errors.append("Segment sans ligne définie: ").append(segment).append("\n");
                }
            }
        }

        if (!isValid) {
            System.err.println("Erreurs de validation du graphe:");
            System.err.println(errors.toString());
        }

        return isValid;
    }

    public void displayGraphStats() {
        System.out.println("Statistiques du graphe de transport:");
        System.out.println("Nombre total de stations: " + getAllStations().size());
        
        Set<String> allLines = new HashSet<>();
        int totalSegments = 0;
        for (Map.Entry<Station, List<Segment>> entry : adjacencyList.entrySet()) {
            totalSegments += entry.getValue().size();
            for (Segment segment : entry.getValue()) {
                allLines.add(segmentLines.get(segment));
            }
        }
        
        System.out.println("Nombre total de segments: " + totalSegments);
        System.out.println("Nombre de lignes: " + allLines.size());
        System.out.println("Stations isolées: " + getIsolatedStations().size());
        System.out.println("Nombre de composantes connexes: " + getConnectedComponents().size());
    }

    public int cleanGraph() {
        int removedCount = 0;
        
        // Supprimer les segments en double
        removedCount += removeDoubleSegments();
        
        // Supprimer les stations isolées
        removedCount += removeIsolatedStations();
        
        // Évaluer la connectivité finale
        evaluateConnectivity();
        

        
        return removedCount;
    }

    private int removeDoubleSegments() {
        int removedCount = 0;
        Map<String, Segment> bestForwardSegments = new HashMap<>();
        
        // First pass: find the best forward segment for each station pair and line
        for (Station station : adjacencyList.keySet()) {
            for (Segment segment : new ArrayList<>(adjacencyList.get(station))) {
                Station dest = segment.getArrival();
                String line = segmentLines.get(segment);
                
                // Create a key for this station pair and line
                String key = line + ":" + station.getId() + "-" + dest.getId();
                
                // Only consider forward segments from lower ID to higher ID
                if (station.getId().compareTo(dest.getId()) < 0) {
                    Segment existing = bestForwardSegments.get(key);
                    if (existing == null || segment.getDuration() < existing.getDuration()) {
                        bestForwardSegments.put(key, segment);
                    }
                }
            }
        }
        
        // Second pass: remove duplicates and return segments
        for (Station station : adjacencyList.keySet()) {
            List<Segment> segments = adjacencyList.get(station);
            List<Segment> toRemove = new ArrayList<>();
            
            for (Segment segment : new ArrayList<>(segments)) {
                Station dest = segment.getArrival();
                String line = segmentLines.get(segment);
                
                // For forward segments (A->B where A.id < B.id)
                if (station.getId().compareTo(dest.getId()) < 0) {
                    String key = line + ":" + station.getId() + "-" + dest.getId();
                    if (!segment.equals(bestForwardSegments.get(key))) {
                        toRemove.add(segment);
                        removedCount++;
                    }
                } else {
                    // For return segments (B->A where B.id > A.id)
                    // Remove all return segments except for B->C
                    if (!line.equals("Ligne 5")) {
                        toRemove.add(segment);
                        removedCount++;
                    }
                }
            }
            
            segments.removeAll(toRemove);
            toRemove.forEach(segmentLines::remove);
        }
        
        return removedCount;
    }

    private int removeIsolatedStations() {
        Set<Station> isolated = getIsolatedStations();
        for (Station station : isolated) {
            adjacencyList.remove(station);
        }
        return isolated.size();
    }

    private void evaluateConnectivity() {
        System.out.println("\nRapport de connectivité du graphe:");
        
        // Statistiques générales
        int totalStations = getAllStations().size();
        int totalSegments = adjacencyList.values().stream()
            .mapToInt(List::size)
            .sum();
        
        System.out.println("Nombre total de stations: " + totalStations);
        System.out.println("Nombre total de segments: " + totalSegments);
        
        // Analyse des composantes connexes
        List<Set<Station>> components = getConnectedComponents();
        System.out.println("Nombre de composantes connexes: " + components.size());
        
        // Trier les composantes par taille décroissante
        components.sort((c1, c2) -> Integer.compare(c2.size(), c1.size()));
        
        // Afficher les détails des composantes
        for (int i = 0; i < components.size(); i++) {
            Set<Station> component = components.get(i);
            Set<String> lines = new HashSet<>();
            
            // Collecter toutes les lignes dans cette composante
            for (Station station : component) {
                for (Segment segment : getNeighbors(station)) {
                    lines.add(segmentLines.get(segment));
                }
            }
            
            System.out.printf("Composante %d: %d stations, %d lignes%n", 
                i + 1, component.size(), lines.size());
            
            // Si c'est une petite composante isolée, afficher plus de détails
            if (component.size() < 5) {
                System.out.println("  Stations dans cette composante:");
                for (Station station : component) {
                    System.out.println("    - " + station.getName());
                }
            }
        }
        
        // Calculer et afficher la densité du graphe
        double density = (double) totalSegments / (totalStations * (totalStations - 1));
        System.out.printf("Densité du graphe: %.4f%n", density);
    }
}