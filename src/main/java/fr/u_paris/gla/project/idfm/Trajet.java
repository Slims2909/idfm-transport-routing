
package fr.u_paris.gla.project.idfm;

public class Trajet {
    private Station depart;
    private Station arrivee;
    private double distance;  // Distance en kilomètres
    private double temps;  // Temps en minutes

    public Trajet(Station depart, Station arrivee, double distance, double temps) {
        this.depart = depart;
        this.arrivee = arrivee;
        this.distance = distance;
        this.temps = temps;
    }

    public Station getDepart() {
        return depart;
    }

    public Station getArrivee() {
        return arrivee;
    }

    public double getDistance() {
        return distance;
    }

    public double getTemps() {
        return temps;
    }
}
