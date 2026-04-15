package fr.u_paris.gla.project.idfm;

import java.util.ArrayList;
import java.util.List;

public class UnidentifiedStopEntry extends StopEntry {
    private final List<StopEntry> candidates;

    public UnidentifiedStopEntry(double longitude, double latitude) {
        super("UNIDENTIFIED", longitude, latitude);
        this.candidates = new ArrayList<>();
    }

    public void addCandidate(StopEntry candidate) {
        if (!candidates.contains(candidate)) {
            candidates.add(candidate);
        }
    }

    public StopEntry resolve() {
        if (candidates.isEmpty()) {
            return null;
        }
        // Pour l'instant, on prend simplement le premier candidat
        // On pourrait améliorer cela en prenant le plus proche ou en utilisant d'autres critères
        return candidates.get(0);
    }

    @Override
    public String toString() {
        return String.format("UNIDENTIFIED (%.6f, %.6f) - %d candidates", 
            longitude, latitude, candidates.size());
    }
}
