package fr.u_paris.gla.project.idfm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;

public class GrapheTransportTest {
    private GrapheTransport graphe;
    private Station stationA;
    private Station stationB;
    private Station stationC;
    
    @BeforeEach
    void setUp() {
        graphe = new GrapheTransport();
        
        // Créer les stations
        stationA = new Station("1", "Châtelet", 48.8586, 2.3488);
        stationB = new Station("2", "Gare du Nord", 48.8809, 2.3553);
        stationC = new Station("3", "Nation", 48.8483, 2.3962);
    }
    
    @Test
    void testNettoyageDoublons() {
        // Créer les segments initiaux
        Segment segment1 = new Segment(stationA, stationB, 300.0, 2500.0, "Ligne 4"); // 5 min, 2.5km
        Segment segment2 = new Segment(stationB, stationC, 420.0, 3500.0, "Ligne 5"); // 7 min, 3.5km
        Segment segment3 = new Segment(stationA, stationC, 600.0, 5000.0, "Ligne 1"); // 10 min, 5km
        
        // Ajouter les segments
        graphe.addSegment(segment1);
        graphe.addSegment(segment2);
        graphe.addSegment(segment3);
        
        // Vérifier l'état initial
        assertEquals(3, graphe.getSegmentCount(), "Le graphe devrait avoir 3 segments initialement");
        
        // Ajouter un segment en double pour tester le nettoyage
        Segment segment1Bis = new Segment(stationA, stationB, 360.0, 2500.0, "Ligne 4"); // même trajet mais plus lent
        graphe.addSegment(segment1Bis);
        
        // Vérifier que le segment a bien été ajouté
        assertEquals(4, graphe.getSegmentCount(), "Le graphe devrait avoir 4 segments après l'ajout du doublon");
        
        // Nettoyer le graphe
        graphe.cleanGraph();
        
        
        // Vérifier que le nettoyage a bien fonctionné
        assertEquals(3, graphe.getSegmentCount(), "Le graphe devrait avoir 3 segments après nettoyage");
        
        // Vérifier que c'est bien le segment le plus rapide qui a été gardé
        Set<Segment> segmentsAB = graphe.getNeighbors(stationA).stream()
                .filter(s -> s.getStation2().equals(stationB) || s.getStation1().equals(stationB))
                .collect(Collectors.toSet());
        
        assertEquals(1, segmentsAB.size(), "Il devrait y avoir un seul segment entre A et B");
        assertEquals(300.0, segmentsAB.iterator().next().getDuration(), 
                "Le segment gardé devrait être celui avec la durée la plus courte");
    }
    
    @Test
    void testConnectivite() {
        // Créer et ajouter les segments
        Segment segment1 = new Segment(stationA, stationB, 300.0, 2500.0, "Ligne 4");
        Segment segment2 = new Segment(stationB, stationC, 420.0, 3500.0, "Ligne 5");
        graphe.addSegment(segment1);
        graphe.addSegment(segment2);
        
        // Vérifier la structure
        assertTrue(graphe.isConnected(), "Le graphe devrait être connexe");
        assertTrue(graphe.getIsolatedStations().isEmpty(), "Il ne devrait pas y avoir de stations isolées");
        
        // Vérifier les voisins
        Set<Station> voisinsAStations = graphe.getNeighborStations(stationA);
        assertTrue(voisinsAStations.contains(stationB), "La station B devrait être voisine de A");
        assertFalse(voisinsAStations.contains(stationC), "La station C ne devrait pas être voisine directe de A");
    }
    
    @Test
    void testRechercheParLigne() {
        // Créer et ajouter les segments
        Segment segment1 = new Segment(stationA, stationB, 300.0, 2500.0, "Ligne 4");
        Segment segment2 = new Segment(stationB, stationC, 420.0, 3500.0, "Ligne 4"); // Même ligne
        graphe.addSegment(segment1);
        graphe.addSegment(segment2);
        
        // Tester la recherche de voisins sur la ligne 4
        Set<Station> voisinsALigne4 = graphe.getNeighborStationsOnLine(stationA, "Ligne 4");
        assertEquals(1, voisinsALigne4.size(), "Il devrait y avoir un seul voisin sur la ligne 4");
        assertTrue(voisinsALigne4.contains(stationB), "Le voisin sur la ligne 4 devrait être la station B");
        
        Set<Station> voisinsBLigne4 = graphe.getNeighborStationsOnLine(stationB, "Ligne 4");
        assertEquals(2, voisinsBLigne4.size(), "La station B devrait avoir deux voisins sur la ligne 4");
        assertTrue(voisinsBLigne4.contains(stationA), "La station A devrait être voisine de B sur la ligne 4");
        assertTrue(voisinsBLigne4.contains(stationC), "La station C devrait être voisine de B sur la ligne 4");
    }
}