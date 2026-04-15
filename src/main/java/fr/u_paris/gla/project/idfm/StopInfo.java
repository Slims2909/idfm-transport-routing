package fr.u_paris.gla.project.idfm;

public class StopInfo {
    private final String name;
    private final double lat;
    private final double lon;
    
    public StopInfo(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }
    
    public String getName() {
        return name;
    }
    
    public double getLat() {
        return lat;
    }
    
    public double getLon() {
        return lon;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%.6f, %.6f)", name, lat, lon);
    }
}
