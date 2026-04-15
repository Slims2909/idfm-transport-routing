package fr.u_paris.gla.project.idfm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import fr.u_paris.gla.project.utils.CSVTools;
import fr.u_paris.gla.project.idfm.CSVFormatter;

public class IDFMDataDownloader {
    // URLs de l'API IDFM v2.1
    private static final String TRACE_FILE_URL = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/traces-des-lignes-de-transport-en-commun-idfm/exports/csv?lang=fr&timezone=Europe%2FBerlin&use_labels=true&delimiter=%3B";
    private static final String STOPS_FILE_URL = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/arrets-lignes/exports/csv?lang=fr&timezone=Europe%2FBerlin&use_labels=true&delimiter=%3B";

    public static void main(String[] args) {
        try {
            // Créer le dossier data s'il n'existe pas
            Path dataDir = Paths.get("data");
            System.out.println("Création du dossier data...");
            try {
                Files.createDirectories(dataDir);
                System.out.println("Dossier data créé avec succès dans : " + dataDir.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Impossible de créer le dossier data: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            // Télécharger et formater traces.csv
            try {
                Path rawTracesPath = dataDir.resolve("traces_raw.csv");
                Path tracesPath = dataDir.resolve("traces.csv");
                Path stopsPath = dataDir.resolve("stops.csv");
                Path networkPath = dataDir.resolve("network.csv");
                
                System.out.println("\nTéléchargement du fichier traces.csv");
                System.out.println("URL: " + TRACE_FILE_URL);
                System.out.println("Destination: " + rawTracesPath.toAbsolutePath());
                
                CSVTools.downloadCSVFromURL(TRACE_FILE_URL, rawTracesPath.toString());
                System.out.println("Fichier brut téléchargé avec succès");
                
                System.out.println("\nTéléchargement du fichier stops.csv");
                System.out.println("URL: " + STOPS_FILE_URL);
                System.out.println("Destination: " + stopsPath.toAbsolutePath());
                
                CSVTools.downloadCSVFromURL(STOPS_FILE_URL, stopsPath.toString());
                System.out.println("Fichier brut téléchargé avec succès");
                
                System.out.println("Formatage des données...");
                CSVFormatter.formatNetworkData(rawTracesPath, stopsPath, networkPath);
                Files.delete(rawTracesPath);
                System.out.println("Données téléchargées et formatées avec succès dans : " + dataDir.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Erreur lors du traitement des données: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("Erreur générale: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
