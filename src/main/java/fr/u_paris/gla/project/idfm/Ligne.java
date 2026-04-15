package fr.u_paris.gla.project.idfm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ligne {
    private final String id;
    private final String name;
    private final String type;  // métro, RER, bus, etc.
    private final String color;  // code couleur de la ligne
    private final List<Segment> segments;
    
    public Ligne(String id, String name) {
        this(id, name, "", "");
    }
    
    public Ligne(String id, String name, String type, String color) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.color = color;
        this.segments = new ArrayList<>();
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public String getColor() {
        return color;
    }
    
    public void addSegment(Segment segment) {
        segments.add(segment);
    }
    
    public List<Segment> getSegments() {
        return Collections.unmodifiableList(segments);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ligne ligne = (Ligne) o;
        return id.equals(ligne.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Ligne{id='%s', name='%s', type='%s'}", id, name, type);
    }
}
