package fr.u_paris.gla.project.idfm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * 
 */
public class GrapheTransportTest {
    
    @Test
    public void testGrapheTransport() {
        // Créer le graphe
        GrapheTransport graphe = new GrapheTransport();
        
        // Créer quelques stations de test
        Station stationA = new Station("1", "Châtelet", 48.8586, 2.3488);
        Station stationB = new Station("2", "Gare du Nord", 48.8809, 2.3553);
        Station stationC = new Station("3", "Nation", 48.8483, 2.3962);
        
        // Créer des segments
        Segment segment1 = new Segment(stationA, stationB, 300, 2500); // 5 min, 2.5km
        Segment segment2 = new Segment(stationB, stationC, 420, 3500); // 7 min, 3.5km
        Segment segment3 = new Segment(stationA, stationC, 600, 5000); // 10 min, 5km
        
        // Ajouter les segments avec leurs lignes
        graphe.addSegment(segment1, "Ligne 4");
        graphe.addSegment(segment2, "Ligne 5");
        graphe.addSegment(segment3, "Ligne 1");
        
        // Ajouter un segment en double pour tester le nettoyage
        Segment segment1Bis = new Segment(stationA, stationB, 360, 2500); // même trajet mais plus lent
        graphe.addSegment(segment1Bis, "Ligne 4");
        
        // Afficher les stats initiales
        System.out.println("État initial du graphe :");
        graphe.displayGraphStats();
        
        // Nettoyer le graphe
        int elementsSupprimes = graphe.cleanGraph();
        System.out.println("\nÉléments supprimés : " + elementsSupprimes);
        
        // Vérifier que quatre segments ont été supprimés:
        // 1. Le segment1Bis (A->B plus lent)
        // 2. Son retour (B->A plus lent)
        // 3. Le retour du segment1 (B->A)
        // 4. Le retour du segment1Bis (B->A)
        assertEquals(4, elementsSupprimes);
        
        // Vérifier les connexions
        List<Segment> voisinsA = graphe.getNeighbors(stationA);
        List<Segment> voisinsB = graphe.getNeighbors(stationB);
        
        // Station A doit avoir 2 segments (vers B et C)
        assertEquals(2, voisinsA.size());
        // Station B doit avoir 1 segment (vers C)
        assertEquals(1, voisinsB.size());
        
        // Vérifier que le segment le plus rapide a été gardé
        assertEquals(300, voisinsA.get(0).getDuration()); // Le segment avec durée = 300s a été gardé
        
        // Vérifier la structure
        assertTrue(graphe.isConnected(), "Le graphe devrait être connexe");
        assertEquals(0, graphe.getIsolatedStations().size(), "Il ne devrait pas y avoir de stations isolées");
        
        // Tester la recherche de voisins
        List<Station> voisinsAStations = graphe.getNeighborStations(stationA);
        assertTrue(voisinsAStations.contains(stationB), "La station B devrait être voisine de A");
        assertTrue(voisinsAStations.contains(stationC), "La station C devrait être voisine de A");
        
        // Tester la recherche de voisins sur une ligne spécifique
        List<Station> voisinsALigne4 = graphe.getNeighborStationsOnLine(stationA, "Ligne 4");
        assertEquals(1, voisinsALigne4.size(), "Il devrait y avoir un seul voisin sur la ligne 4");
        assertEquals(stationB, voisinsALigne4.get(0), "Le voisin sur la ligne 4 devrait être la station B");
    }
}