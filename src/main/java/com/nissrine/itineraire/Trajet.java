package com.nissrine.itineraire;

import java.util.ArrayList;
import java.util.List;

public class Trajet {
    public static class Etape {
        public String ligne;
        public String depart;
        public String arrivee;
        public String horaireDepart;
        public String horaireArrivee;
        public int duree;

        public Etape(String ligne, String depart, String arrivee, String horaireDepart, String horaireArrivee, int duree) {
            this.ligne = ligne;
            this.depart = depart;
            this.arrivee = arrivee;
            this.horaireDepart = horaireDepart;
            this.horaireArrivee = horaireArrivee;
            this.duree = duree;
        }
    }

    public List<Etape> etapes = new ArrayList<>();

    public void ajouterEtape(Etape etape) {
        etapes.add(etape);
    }

    public int dureeTotale() {
        return etapes.stream().mapToInt(e -> e.duree).sum();
    }
}
