package fr.u_paris.gla.project.idfm;

public class Stop {
    private final String id;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final String type;  // métro, RER, bus, etc.
    
    public Stop(String id, String name, double latitude, double longitude, String type) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getType() { return type; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stop stop = (Stop) o;
        return id.equals(stop.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Stop{id='%s', name='%s', type='%s'}", id, name, type);
    }
}
