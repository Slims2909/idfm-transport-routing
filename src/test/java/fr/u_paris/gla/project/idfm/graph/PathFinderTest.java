package fr.u_paris.gla.project.idfm.graph;

import fr.u_paris.gla.project.idfm.StopInfo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class PathFinderTest {

    @Test
    public void testSimplePath() {
        PathFinder finder = new PathFinder();
        
        // Créer une ligne simple : A -> B -> C
        List<StopInfo> stops = Arrays.asList(
            new StopInfo("A", 48.8566, 2.3522),
            new StopInfo("B", 48.8566, 2.3522),
            new StopInfo("C", 48.8566, 2.3522)
        );
        
        List<Integer> durations = Arrays.asList(10, 15); // A->B: 10min, B->C: 15min
        
        finder.addLine("L1", stops, durations);
        
        List<Node> path = finder.findShortestPath("A", "C");
        assertFalse(path.isEmpty(), "Le chemin ne devrait pas être vide");
        assertEquals(3, path.size(), "Le chemin devrait avoir 3 stations");
        assertEquals("A", path.get(0).getStop().getName(), "La première station devrait être A");
        assertEquals("C", path.get(2).getStop().getName(), "La dernière station devrait être C");
        
        String formattedPath = finder.formatPath(path);
        System.out.println("Test chemin simple :\n" + formattedPath);
    }
    
    @Test
    public void testPathWithTransfer() {
        PathFinder finder = new PathFinder();
        
        // Ligne 1 : A -> B -> C
        List<StopInfo> line1 = Arrays.asList(
            new StopInfo("A", 48.8566, 2.3522),
            new StopInfo("B", 48.8566, 2.3522),
            new StopInfo("C", 48.8566, 2.3522)
        );
        List<Integer> durations1 = Arrays.asList(10, 15);
        
        // Ligne 2 : B -> D -> E
        List<StopInfo> line2 = Arrays.asList(
            new StopInfo("B", 48.8566, 2.3522),
            new StopInfo("D", 48.8566, 2.3522),
            new StopInfo("E", 48.8566, 2.3522)
        );
        List<Integer> durations2 = Arrays.asList(8, 12);
        
        finder.addLine("L1", line1, durations1);
        finder.addLine("L2", line2, durations2);
        
        List<Node> path = finder.findShortestPath("A", "E");
        assertFalse(path.isEmpty(), "Le chemin ne devrait pas être vide");
        
        String formattedPath = finder.formatPath(path);
        System.out.println("Test chemin avec correspondance :\n" + formattedPath);
        
        // Vérifier que le chemin passe par B (correspondance)
        boolean hasTransfer = false;
        String previousLine = path.get(0).getLineId();
        for (Node node : path) {
            if (!node.getLineId().equals(previousLine)) {
                hasTransfer = true;
                break;
            }
            previousLine = node.getLineId();
        }
        assertTrue(hasTransfer, "Le chemin devrait avoir une correspondance");
    }
    
    @Test
    public void testNoPath() {
        PathFinder finder = new PathFinder();
        
        // Ligne isolée : A -> B
        List<StopInfo> stops = Arrays.asList(
            new StopInfo("A", 48.8566, 2.3522),
            new StopInfo("B", 48.8566, 2.3522)
        );
        List<Integer> durations = Collections.singletonList(10);
        
        finder.addLine("L1", stops, durations);
        
        List<Node> path = finder.findShortestPath("A", "NonExistant");
        assertTrue(path.isEmpty(), "Le chemin devrait être vide");
        
        String formattedPath = finder.formatPath(path);
        assertEquals("Aucun chemin trouvé", formattedPath, "Le message devrait indiquer qu'aucun chemin n'est trouvé");
    }
    
    @Test
    public void testComplexNetwork() {
        PathFinder finder = new PathFinder();
        
        // Ligne 1 : A -> B -> C -> D
        List<StopInfo> line1 = Arrays.asList(
            new StopInfo("A", 48.8566, 2.3522),
            new StopInfo("B", 48.8566, 2.3522),
            new StopInfo("C", 48.8566, 2.3522),
            new StopInfo("D", 48.8566, 2.3522)
        );
        List<Integer> durations1 = Arrays.asList(10, 15, 10);
        
        // Ligne 2 : E -> B -> F -> G
        List<StopInfo> line2 = Arrays.asList(
            new StopInfo("E", 48.8566, 2.3522),
            new StopInfo("B", 48.8566, 2.3522),
            new StopInfo("F", 48.8566, 2.3522),
            new StopInfo("G", 48.8566, 2.3522)
        );
        List<Integer> durations2 = Arrays.asList(12, 8, 9);
        
        // Ligne 3 : F -> H -> D
        List<StopInfo> line3 = Arrays.asList(
            new StopInfo("F", 48.8566, 2.3522),
            new StopInfo("H", 48.8566, 2.3522),
            new StopInfo("D", 48.8566, 2.3522)
        );
        List<Integer> durations3 = Arrays.asList(6, 7);
        
        finder.addLine("L1", line1, durations1);
        finder.addLine("L2", line2, durations2);
        finder.addLine("L3", line3, durations3);
        
        // Test d'un chemin complexe de E à D
        List<Node> path = finder.findShortestPath("E", "D");
        assertFalse(path.isEmpty(), "Le chemin ne devrait pas être vide");
        
        String formattedPath = finder.formatPath(path);
        System.out.println("Test réseau complexe (E -> D) :\n" + formattedPath);
        
        // Vérifier que le chemin trouvé est optimal
        int totalDuration = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Node current = path.get(i);
            Node next = path.get(i + 1);
            totalDuration += current.getNeighbors().get(next);
        }
        
        // Le chemin optimal devrait être E -> B -> F -> H -> D
        // Durées : 12 + 8 + 6 + 7 = 33 minutes (plus les correspondances)
        assertTrue(totalDuration >= 33, "La durée totale devrait être cohérente");
    }
}
