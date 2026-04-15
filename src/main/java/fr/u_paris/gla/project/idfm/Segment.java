package fr.u_paris.gla.project.idfm;

/**
 * Représente un segment (liaison) entre deux stations
 */
public class Segment {
    private final Station station1;
    private final Station station2;
    private final double duration; // en minutes
    private final double distance; // en mètres
    private final String lineId;
    
    public Segment(Station station1, Station station2, double duration, double distance, String lineId) {
        this.station1 = station1;
        this.station2 = station2;
        this.duration = duration;
        this.distance = distance;
        this.lineId = lineId;
    }
    
    public Station getStation1() {
        return station1;
    }
    
    public Station getStation2() {
        return station2;
    }
    
    public double getDuration() {
        return duration;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public String getLineId() {
        return lineId;
    }
    
    /**
     * Retourne l'autre station du segment
     */
    public Station getOtherStation(Station station) {
        if (station.equals(station1)) return station2;
        if (station.equals(station2)) return station1;
        throw new IllegalArgumentException("La station n'appartient pas à ce segment");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Segment segment = (Segment) o;
        // Deux segments sont égaux s'ils relient les mêmes stations sur la même ligne,
        // indépendamment de l'ordre des stations
        return ((station1.equals(segment.station1) && station2.equals(segment.station2)) ||
                (station1.equals(segment.station2) && station2.equals(segment.station1))) &&
               lineId.equals(segment.lineId);
    }
    
    @Override
    public int hashCode() {
        // L'ordre des stations ne doit pas affecter le hashCode
        int stationsHash = station1.hashCode() + station2.hashCode();
        return java.util.Objects.hash(stationsHash, lineId);
    }
    
    /**
     * Compare deux segments en tenant compte de la durée
     */
    public boolean equalsWithDuration(Segment other) {
        return equals(other) && duration == other.duration;
    }
    
    /**
     * Vérifie si ce segment est plus rapide qu'un autre segment reliant les mêmes stations sur la même ligne
     */
    public boolean isFasterThan(Segment other) {
        return equals(other) && duration < other.duration;
    }
    
    @Override
    public String toString() {
        return String.format("Segment[%s -> %s, ligne=%s, durée=%.1f]", 
            station1.getName(), station2.getName(), lineId, duration);
    }
}