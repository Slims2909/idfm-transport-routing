package fr.u_paris.gla.project.idfm;

public class Segment {
    private Station departure;
    private Station arrival;
    private int duration; // in seconds
    private double distance; // in meters

    public Segment(Station departure, Station arrival, int duration, double distance) {
        this.departure = departure;
        this.arrival = arrival;
        this.duration = duration;
        this.distance = distance;
    }

    // Getters
    public Station getDeparture() { return departure; }
    public Station getArrival() { return arrival; }
    public int getDuration() { return duration; }
    public double getDistance() { return distance; }

    @Override
    public String toString() {
        return "Segment{" +
                "from=" + departure.getName() +
                ", to=" + arrival.getName() +
                ", duration=" + duration +
                "s, distance=" + distance +
                "m}";
    }
}