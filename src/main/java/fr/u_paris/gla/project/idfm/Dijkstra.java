package fr.u_paris.gla.project.idfm;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Dijkstra {

    public static Map<Station, Double> calculerChemin(GrapheTransport graphe, Station depart, Station arrivee, boolean optimiserParTemps) {
        Map<Station, Double> distances = new HashMap<>();  // Contient la distance minimale vers chaque station
        Map<Station, Station> predecesseurs = new HashMap<>();  // Contient les prédécesseurs de chaque station
        PriorityQueue<Station> filePriorite = new PriorityQueue<>(Comparator.comparing(distances::get));  // File de priorité pour gérer les stations à explorer

        for (Station station : graphe.getAllStations()) {
            distances.put(station, Double.POSITIVE_INFINITY);
        }
        distances.put(depart, 0.0);

        filePriorite.add(depart);  // Ajouter la station de départ dans la file de priorité

        while (!filePriorite.isEmpty()) {
            Station current = filePriorite.poll();  // Récupérer la station avec la distance la plus faible

            // Si la station actuelle est la station d'arrivée, on termine l'algorithme
            if (current.equals(arrivee)) {
                break;
            }

            // Exploration des voisins de la station actuelle (ici, ce sont des segments)
            for (Segment segment : graphe.getNeighbors(current)) {
                Station voisin = segment.getArrival();
                double poids = optimiserParTemps ? segment.getDuration() : segment.getDistance();  // Choisir entre temps ou distance
                double nouvelleDistance = distances.get(current) + poids;

                // Si la nouvelle distance est plus courte, on met à jour la distance et le prédécesseur
                if (nouvelleDistance < distances.get(voisin)) {
                    distances.put(voisin, nouvelleDistance);
                    predecesseurs.put(voisin, current);
                    filePriorite.add(voisin);
                }
            }
        }

        return distances;
    }
}
