package fr.u_paris.gla.project.idfm;


public class TestDijkstra {
    public static void main(String[] args) {
        // Création du graphe de transport
        GrapheTransport graphe = new GrapheTransport();
        
        // Création des stations
        Station a = new Station("A", "Station A", 48.8566, 2.3522); // Paris
        Station b = new Station("B", "Station B", 48.8570, 2.3670); // Station B
        Station c = new Station("C", "Station C", 48.8565, 2.3800); // Station C
        
        // Ajout des stations dans le graphe
        graphe.addStation(a);
        graphe.addStation(b);
        graphe.addStation(c);
        
        // Création des segments (trajets)
        Segment ab = new Segment(a, b, 10, 500);  // Distance: 500 m, Temps: 10 min
        Segment bc = new Segment(b, c, 6, 300);   // Distance: 300 m, Temps: 6 min
        Segment ac = new Segment(a, c, 12, 600);  // Distance: 600 m, Temps: 12 min
        
        // Ajout des segments dans le graphe
        graphe.addSegment(ab, "Ligne 1");
        graphe.addSegment(bc, "Ligne 2");
        graphe.addSegment(ac, "Ligne 3");
        
        // Calcul du chemin optimisé par temps
        Map<Station, Double> distances = Dijkstra.calculerChemin(graphe, a, c, true);

        // Affichage de la distance minimale
        System.out.println("Distance minimale entre A et C (optimisée par temps) : " + distances.get(c));
    }
}
