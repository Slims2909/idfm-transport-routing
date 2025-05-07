Implémentation de Dijkstra pour le Réseau de Transport


Description


Ce projet implémente l'algorithme de Dijkstra pour trouver le plus court chemin entre deux stations dans un réseau de transport. Le critère d'optimisation peut être la distance ou le temps.

Classes principales

Station : Représente une station avec un id, name, latitude, et longitude.

Segment : Représente un trajet entre deux stations avec departure, arrival, duration, et distance.

GrapheTransport : Modélise le réseau de transport avec des stations et des segments.

Dijkstra : Implémente l'algorithme de Dijkstra pour calculer le plus court chemin entre deux stations.

CSVParser et CSVStreamProvider : Pour charger les données du réseau à partir de fichiers CSV.

Fonctionnalités
Calcul du plus court chemin entre deux stations.

Optimisation en fonction du temps ou de la distance.

Affichage des statistiques du graphe (stations isolées, densité, etc.).

Exemple d'utilisation

GrapheTransport graphe = new GrapheTransport();
graphe.loadFromCSVProvider(provider);

Station depart = new Station("S1", "Station 1", 48.8566, 2.3522);
Station arrivee = new Station("S2", "Station 2", 48.8600, 2.3500);
Map<Station, Double> distances = Dijkstra.calculerChemin(graphe, depart, arrivee, true);