package fr.u_paris.gla.project.idfm;

public class StopEntry {
    protected final String name;
    protected final double longitude;
    protected final double latitude;

    public StopEntry(String name, double longitude, double latitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    @Override
    public String toString() {
        return String.format("%s (%.6f, %.6f)", name, longitude, latitude);
    }
}
