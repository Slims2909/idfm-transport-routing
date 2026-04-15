package fr.u_paris.gla.project.idfm.graph;

import fr.u_paris.gla.project.idfm.StopInfo;
import java.util.*;

public class PathFinder {
    private final Map<String, List<Node>> networkByLine;
    private final Map<String, Node> allNodes; // stopName -> Node

    public PathFinder() {
        this.networkByLine = new HashMap<>();
        this.allNodes = new HashMap<>();
    }

    public void addLine(String lineId, List<StopInfo> stops, List<Integer> durations) {
        List<Node> nodes = new ArrayList<>();
        
        // Créer les nœuds pour chaque arrêt
        for (StopInfo stop : stops) {
            String key = stop.getName() + "_" + lineId;
            Node node = new Node(stop, lineId);
            nodes.add(node);
            allNodes.put(key, node);
        }
        
        // Ajouter les connexions entre les arrêts consécutifs
        for (int i = 0; i < nodes.size() - 1; i++) {
            Node current = nodes.get(i);
            Node next = nodes.get(i + 1);
            int duration = durations.get(i);
            
            // Connexion dans les deux sens avec le même temps
            current.addNeighbor(next, duration);
            next.addNeighbor(current, duration);
        }
        
        // Ajouter des connexions entre les stations de correspondance
        for (Node node : nodes) {
            String stopName = node.getStop().getName();
            for (Map.Entry<String, Node> entry : allNodes.entrySet()) {
                if (entry.getKey().startsWith(stopName + "_") && !entry.getKey().equals(stopName + "_" + lineId)) {
                    // Temps de correspondance fixe de 5 minutes
                    node.addNeighbor(entry.getValue(), 5);
                    entry.getValue().addNeighbor(node, 5);
                }
            }
        }
        
        networkByLine.put(lineId, nodes);
    }

    public List<Node> findShortestPath(String startStation, String endStation) {
        // Trouver tous les nœuds correspondant aux stations de départ et d'arrivée
        List<Node> startNodes = new ArrayList<>();
        List<Node> endNodes = new ArrayList<>();
        
        for (Map.Entry<String, Node> entry : allNodes.entrySet()) {
            if (entry.getValue().getStop().getName().equals(startStation)) {
                startNodes.add(entry.getValue());
            }
            if (entry.getValue().getStop().getName().equals(endStation)) {
                endNodes.add(entry.getValue());
            }
        }
        
        if (startNodes.isEmpty() || endNodes.isEmpty()) {
            return Collections.emptyList();
        }

        // Initialiser les distances
        for (Node node : allNodes.values()) {
            node.setDistance(Integer.MAX_VALUE);
            node.setPrevious(null);
        }

        // File de priorité pour Dijkstra
        PriorityQueue<Node> queue = new PriorityQueue<>();
        
        // Initialiser les nœuds de départ
        for (Node start : startNodes) {
            start.setDistance(0);
            queue.offer(start);
        }

        // Algorithme de Dijkstra
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
            // Si on a atteint un nœud d'arrivée avec la distance minimale
            if (endNodes.contains(current)) {
                return reconstructPath(current);
            }

            for (Map.Entry<Node, Integer> neighbor : current.getNeighbors().entrySet()) {
                Node next = neighbor.getKey();
                int newDist = current.getDistance() + neighbor.getValue();
                
                if (newDist < next.getDistance()) {
                    queue.remove(next);
                    next.setDistance(newDist);
                    next.setPrevious(current);
                    queue.offer(next);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<Node> reconstructPath(Node end) {
        List<Node> path = new ArrayList<>();
        for (Node node = end; node != null; node = node.getPrevious()) {
            path.add(0, node);
        }
        return path;
    }

    public String formatPath(List<Node> path) {
        if (path.isEmpty()) return "Aucun chemin trouvé";
        
        StringBuilder sb = new StringBuilder();
        sb.append("Itinéraire trouvé :\n");
        
        int totalTime = 0;
        String currentLine = path.get(0).getLineId();
        
        for (int i = 0; i < path.size(); i++) {
            Node current = path.get(i);
            
            if (!current.getLineId().equals(currentLine)) {
                sb.append(String.format("Changement : prendre la ligne %s\n", current.getLineId()));
                currentLine = current.getLineId();
            }
            
            sb.append(String.format("- %s", current.getStop().getName()));
            
            if (i < path.size() - 1) {
                int duration = current.getNeighbors().get(path.get(i + 1));
                totalTime += duration;
                sb.append(String.format(" (%d min)\n", duration));
            } else {
                sb.append(" (arrivée)\n");
            }
        }
        
        sb.append(String.format("\nDurée totale : %d minutes\n", totalTime));
        return sb.toString();
    }
}
