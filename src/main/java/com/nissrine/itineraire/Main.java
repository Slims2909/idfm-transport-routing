package com.nissrine.itineraire;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continuer = true;

        while (continuer) {
            System.out.println("\n===== PLANIFICATEUR D’ITINÉRAIRE =====");
            System.out.println("1. Planifier un trajet");
            System.out.println("2. Aide");
            System.out.println("3. Quitter");
            System.out.print("Choix : ");

            String choix = scanner.nextLine().trim();

            switch (choix) {
                case "1":
                    String depart = "";
                    String arrivee = "";
                    String horaireDepart = "";

                    while (depart.isEmpty()) {
                        System.out.print("Entrez la station de départ : ");
                        depart = scanner.nextLine().trim();
                        if (depart.isEmpty()) {
                            System.out.println("⚠️ Station de départ invalide. Veuillez réessayer.");
                        }
                    }

                    while (arrivee.isEmpty()) {
                        System.out.print("Entrez la station d’arrivée : ");
                        arrivee = scanner.nextLine().trim();
                        if (arrivee.isEmpty()) {
                            System.out.println("⚠️ Station d’arrivée invalide. Veuillez réessayer.");
                        }
                    }

                    if (depart.equalsIgnoreCase(arrivee)) {
                        System.out.println("⚠️ Les stations de départ et d’arrivée sont identiques.");
                        break;
                    }

                    while (horaireDepart.isEmpty()) {
                        System.out.print("Entrez l’horaire de départ (ex : 14:30) : ");
                        horaireDepart = scanner.nextLine().trim();

                        if (!horaireDepart.matches("\\d{2}:\\d{2}")) {
                            System.out.println("⚠️ Format invalide. Utilisez HH:MM, par exemple 14:30.");
                            horaireDepart = "";
                        }
                    }

                    String changement = "Capitole"; // station fictive de correspondance

                    Trajet trajet = new Trajet();

                    trajet.ajouterEtape(new Trajet.Etape(
                        "Ligne A",
                        depart,
                        changement,
                        horaireDepart,
                        ajouterMinutes(horaireDepart, 5),
                        5
                    ));

                    trajet.ajouterEtape(new Trajet.Etape(
                        "Ligne B",
                        changement,
                        arrivee,
                        ajouterMinutes(horaireDepart, 5),
                        ajouterMinutes(horaireDepart, 17),
                        12
                    ));

                    System.out.println("\n🔍 Recherche du meilleur itinéraire...");
                    TrajetSimule.afficherTrajet(trajet);

                    System.out.println("\nAppuyez sur Entrée pour revenir au menu...");
                    scanner.nextLine();
                    break;

                case "2":
                    System.out.println("\n📘 AIDE :");
                    System.out.println("- Choisissez l’option 1 pour planifier un trajet.");
                    System.out.println("- Tapez exactement le nom des stations (ex : Jean Jaurès).");
                    System.out.println("- Saisissez l’horaire au format HH:MM (ex : 08:15).");
                    break;

                case "3":
                    System.out.println("👋 Merci d’avoir utilisé le planificateur !");
                    continuer = false;
                    break;

                default:
                    System.out.println("❌ Choix invalide. Veuillez entrer 1, 2 ou 3.");
            }
        }

        scanner.close();
    }

    public static String ajouterMinutes(String heure, int minutes) {
        String[] parts = heure.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);

        m += minutes;
        h += m / 60;
        m = m % 60;
        h = h % 24;

        return String.format("%02d:%02d", h, m);
    }
}
