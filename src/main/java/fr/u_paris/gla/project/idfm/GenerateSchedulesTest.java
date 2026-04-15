package fr.u_paris.gla.project.idfm;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class GenerateSchedulesTest {
    public static void main(String[] args) {
        try {
            System.err.println("Début de la génération des horaires...");
            
            Path networkPath = Paths.get("data/network.csv");
            Path schedulesPath = Paths.get("data/schedules.csv");
            
            // Vérifier que network.csv existe
            if (!Files.exists(networkPath)) {
                throw new RuntimeException("Le fichier network.csv n'existe pas : " + networkPath.toAbsolutePath());
            }
            
            System.err.println("Fichier réseau trouvé : " + networkPath.toAbsolutePath());
            System.err.println("Taille du fichier réseau : " + Files.size(networkPath) + " octets");
            
            // Générer les horaires
            System.err.println("Génération des horaires...");
            ScheduleGenerator.generateSchedules(networkPath, schedulesPath);
            
            // Vérifier que le fichier a été créé
            if (Files.exists(schedulesPath)) {
                System.err.println("Horaires générés avec succès !");
                System.err.println("Fichier créé : " + schedulesPath.toAbsolutePath());
                System.err.println("Taille du fichier : " + Files.size(schedulesPath) + " octets");
            } else {
                throw new RuntimeException("Le fichier schedules.csv n'a pas été créé !");
            }
            
        } catch (Exception e) {
            System.err.println("ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
