package com.nissrine.itineraire;

public class TrajetSimule {
    public static void afficherTrajet(Trajet trajet) {
        System.out.println("🚌 Trajet simulé trouvé !");
        System.out.println("-------------------------------");

        int i = 1;
        for (Trajet.Etape etape : trajet.etapes) {
            System.out.println(i++ + ". 🚇 " + etape.ligne + " — " + etape.depart + " → " + etape.arrivee);
            System.out.println("   ⏱ De " + etape.horaireDepart + " à " + etape.horaireArrivee + " (" + etape.duree + " min)");
        }

        System.out.println("-------------------------------");
        System.out.println("⏳ Durée totale estimée : " + trajet.dureeTotale() + " minutes");
    }
}
