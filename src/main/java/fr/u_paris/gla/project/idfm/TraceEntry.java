/**
 * 
 */
package fr.u_paris.gla.project.idfm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Representation of a transport line
 * 
 * @author Emmanuel Bigeon */
public final class TraceEntry {
    public final String           lname;
    private List<List<StopEntry>> paths = new ArrayList<>();

    /** Create a transport line.
     * 
     * @param lname the name of the line */
    public TraceEntry(String lname) {
        super();
        this.lname = lname;
    }

    // FIXME list of lists are bad practice in direct access...
    /** @return the list of paths */
    public List<List<StopEntry>> getPaths() {
        return Collections.unmodifiableList(paths);
    }

    /**
     * Récupère le nombre total de chemins définis pour cette ligne.
     * @return nombre de chemins
     */
    public int getNumberOfPaths() {
        return paths.size();
    }

    public void addPath(List<StopEntry> path) {
        paths.add(new ArrayList<>(path));
    }

      /**
     * Récupère un chemin spécifique par son index.
     * @param index numéro du chemin (0, 1, ...)
     * @return Liste d'arrêts du chemin
     */
    public List<StopEntry> getPath(int index) {
        if (index < 0 || index >= paths.size()) {
            throw new IndexOutOfBoundsException("Chemin inexistant pour la ligne " + lname);
        }
        return Collections.unmodifiableList(paths.get(index));
    }

    public String getName() {
        return this.lname;
    }

    @Override
    public String toString() {
        return "TraceEntry [ligne=" + lname + ", chemins=" + paths.size() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof TraceEntry))
            return false;
        TraceEntry other = (TraceEntry) obj;
        return Objects.equals(lname, other.lname) && Objects.equals(paths, other.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lname, paths);
    }

}
