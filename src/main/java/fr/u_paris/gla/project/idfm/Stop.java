package fr.u_paris.gla.project.idfm;

public class Stop extends Station {
    private final String type;  // métro, RER, bus, etc.
    
    public Stop(String id, String name, double latitude, double longitude, String type) {
        super(id, name, latitude, longitude);
        this.type = type;
    }
    
    // Getter spécifique à Stop
    public String getType() { return type; }
    
    @Override
    public String toString() {
        return String.format("Stop{id='%s', name='%s', type='%s'}", getId(), getName(), type);
    }
}
