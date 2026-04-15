package fr.u_paris.gla.project.idfm;

import java.util.ArrayList;
import java.util.List;

public class TraceEntry {
    public final String lname;
    private final List<List<StopEntry>> paths;

    public TraceEntry(String lname) {
        this.lname = lname;
        this.paths = new ArrayList<>();
    }

    public List<List<StopEntry>> getPaths() {
        return paths;
    }

    public void addPath(List<StopEntry> path) {
        paths.add(path);
    }

    public String getName() {
        return lname;
    }

    public int getNumberOfPaths() {
        return paths.size();
    }

    @Override
    public String toString() {
        return String.format("Ligne %s - %d chemins", lname, paths.size());
    }
}
