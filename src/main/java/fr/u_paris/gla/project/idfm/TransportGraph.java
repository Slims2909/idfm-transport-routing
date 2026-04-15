package fr.u_paris.gla.project.idfm;

import java.util.*;

/**
 * Représente un graphe de transport en commun
 */
public class TransportGraph {
    protected final Map<Station, Set<Segment>> adjacencyList;
    protected final Set<Station> stations;
    protected final List<Segment> segments;  // Changé en List pour permettre les doublons
    
    public TransportGraph() {
        this.adjacencyList = new HashMap<>();
        this.stations = new HashSet<>();
        this.segments = new ArrayList<>();  // ArrayList au lieu de HashSet
    }
    
    /**
     * Ajoute une station au graphe
     */
    public void addStation(Station station) {
        stations.add(station);
        adjacencyList.putIfAbsent(station, new HashSet<>());
    }
    
    /**
     * Ajoute un segment entre deux stations
     */
    public void addSegment(Segment segment) {
        Station station1 = segment.getStation1();
        Station station2 = segment.getStation2();
        
        // Ajoute les stations si elles n'existent pas
        addStation(station1);
        addStation(station2);
        
        // Ajoute le segment dans les deux sens
        adjacencyList.get(station1).add(segment);
        adjacencyList.get(station2).add(segment);
        segments.add(segment);
    }
    
    /**
     * Retourne tous les segments adjacents à une station
     */
    public Set<Segment> getAdjacentSegments(Station station) {
        return adjacencyList.getOrDefault(station, Collections.emptySet());
    }
    
    /**
     * Retourne toutes les stations du graphe
     */
    public Set<Station> getStations() {
        return Collections.unmodifiableSet(stations);
    }
    
    /**
     * Retourne tous les segments du graphe
     */
    public List<Segment> getSegments() {
        return Collections.unmodifiableList(segments);
    }
    
    /**
     * Vérifie si une station existe dans le graphe
     */
    public boolean hasStation(Station station) {
        return stations.contains(station);
    }
    
    /**
     * Vérifie si un segment existe dans le graphe
     */
    public boolean hasSegment(Segment segment) {
        return segments.contains(segment);
    }
    
    /**
     * Retourne le nombre de stations dans le graphe
     */
    public int getStationCount() {
        return stations.size();
    }
    
    /**
     * Retourne le nombre de segments dans le graphe
     */
    public int getSegmentCount() {
        return segments.size();
    }
    
    protected void clearSegments() {
        segments.clear();
        for (Set<Segment> adjacentSegments : adjacencyList.values()) {
            adjacentSegments.clear();
        }
    }
}
