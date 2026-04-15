package fr.u_paris.gla.project.idfm;

import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

public class Schedule {
    private final String lineId;
    private final List<Integer> junctions;
    private final String startTerminus;
    private final LocalTime departureTime;
    
    public Schedule(String lineId, List<Integer> junctions, String startTerminus, LocalTime departureTime) {
        this.lineId = lineId;
        this.junctions = new ArrayList<>(junctions); // Copie défensive
        this.startTerminus = startTerminus;
        this.departureTime = departureTime;
    }
    
    public String getLineId() {
        return lineId;
    }
    
    public List<Integer> getJunctions() {
        return new ArrayList<>(junctions); // Copie défensive
    }
    
    public String getStartTerminus() {
        return startTerminus;
    }
    
    public LocalTime getDepartureTime() {
        return departureTime;
    }
    
    @Override
    public String toString() {
        return String.format("%s; %s; %s; %s", 
            lineId, 
            junctions.toString().replace(" ", ""), // Format [2,1,3] sans espaces
            startTerminus, 
            departureTime.toString());
    }
}
