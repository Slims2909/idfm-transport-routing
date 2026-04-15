package fr.u_paris.gla.project.idfm;

/**
 * Représente un arrêt de transport en commun
 * Étend la classe Station en ajoutant le type d'arrêt
 */
public class Stop extends Station {
    private final String type;
    
    public Stop(String id, String name, double latitude, double longitude, String type) {
        super(id, name, latitude, longitude);
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return String.format("Stop[id=%s, name=%s, type=%s, lat=%.6f, lon=%.6f]",
                           getId(), getName(), type, getLatitude(), getLongitude());
    }
}
