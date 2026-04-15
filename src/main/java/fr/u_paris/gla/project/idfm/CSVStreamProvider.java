package fr.u_paris.gla.project.idfm;

import java.util.Iterator;
import java.util.List;

public class CSVStreamProvider {
    private final Iterator<TraceEntry> traces;
    private int currentPathIndex;
    private TraceEntry currentTrace;
    private boolean hasNext;

    public CSVStreamProvider(Iterator<TraceEntry> traces) {
        this.traces = traces;
        this.currentPathIndex = 0;
        if (traces.hasNext()) {
            this.currentTrace = traces.next();
            this.hasNext = true;
        } else {
            this.hasNext = false;
        }
    }

    public boolean hasNext() {
        return hasNext;
    }

    public String[] next() {
        if (!hasNext) {
            throw new IllegalStateException("No more elements");
        }

        List<StopEntry> currentPath = currentTrace.getPaths().get(currentPathIndex);
        String[] result = new String[]{
            currentTrace.lname,
            String.valueOf(currentPathIndex),
            String.valueOf(currentPath.size())
        };

        // Préparer pour le prochain appel
        currentPathIndex++;
        if (currentPathIndex >= currentTrace.getPaths().size()) {
            currentPathIndex = 0;
            if (traces.hasNext()) {
                currentTrace = traces.next();
            } else {
                hasNext = false;
            }
        }

        return result;
    }
}
