package fr.u_paris.gla.project.idfm.graph;

import fr.u_paris.gla.project.idfm.StopInfo;
import java.util.*;

public class Node implements Comparable<Node> {
    private final StopInfo stop;
    private final Map<Node, Integer> neighbors; // Node -> weight (in minutes)
    private int distance = Integer.MAX_VALUE;
    private Node previous = null;
    private final String lineId;

    public Node(StopInfo stop, String lineId) {
        this.stop = stop;
        this.lineId = lineId;
        this.neighbors = new HashMap<>();
    }

    public void addNeighbor(Node node, int weight) {
        neighbors.put(node, weight);
    }

    public Map<Node, Integer> getNeighbors() {
        return Collections.unmodifiableMap(neighbors);
    }

    public StopInfo getStop() {
        return stop;
    }

    public String getLineId() {
        return lineId;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public Node getPrevious() {
        return previous;
    }

    public void setPrevious(Node previous) {
        this.previous = previous;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.distance, other.distance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(stop, node.stop) && Objects.equals(lineId, node.lineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stop, lineId);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", stop.getName(), lineId);
    }
}
