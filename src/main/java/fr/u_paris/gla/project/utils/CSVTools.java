package fr.u_paris.gla.project.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

public class CSVTools {
    private static final int TIMEOUT = 30000; // 30 secondes
    private static final int BUFFER_SIZE = 8192;

    /**
     * Télécharge un fichier CSV depuis une URL et le sauvegarde localement
     */
    public static void downloadCSVFromURL(String urlStr, String outputPath) throws IOException {
        System.err.println("DEBUG: Tentative de téléchargement depuis : " + urlStr);
        
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "text/csv");
        conn.setRequestProperty("Accept-Encoding", "gzip");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");

        int responseCode = conn.getResponseCode();
        System.err.println("DEBUG: Code de réponse HTTP : " + responseCode);

        if (responseCode != HttpURLConnection.HTTP_OK) {
            String errorMessage = "Erreur HTTP " + responseCode;
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                StringBuilder errorResponse = new StringBuilder();
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine).append("\n");
                }
                errorMessage += "\nRéponse du serveur : " + errorResponse.toString();
            }
            System.err.println("DEBUG: " + errorMessage);
            throw new IOException(errorMessage);
        }

        // Créer le dossier parent si nécessaire
        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs();
        System.err.println("DEBUG: Dossier parent créé : " + outputFile.getParentFile().getAbsolutePath());

        // Copier le contenu dans le fichier de sortie
        try (InputStream in = getInputStream(conn);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8))) {
            
            int lineCount = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
                lineCount++;
                if (lineCount % 1000 == 0) {
                    System.err.println("DEBUG: " + lineCount + " lignes écrites...");
                }
            }
            System.err.println("DEBUG: Total de " + lineCount + " lignes écrites");
        }
        
        System.err.println("DEBUG: Fichier téléchargé avec succès : " + outputPath);
    }

    /**
     * Retourne le bon InputStream en fonction de l'encodage de la réponse
     */
    private static InputStream getInputStream(HttpURLConnection conn) throws IOException {
        InputStream inputStream = conn.getInputStream();
        String contentEncoding = conn.getContentEncoding();
        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            return new GZIPInputStream(inputStream);
        }
        return inputStream;
    }

    public static void readCSVFromURL(String urlStr, Consumer<String[]> lineProcessor) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "text/csv");
        conn.setRequestProperty("Accept-Encoding", "gzip");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream(conn), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {
            // Skip header
            csvReader.readNext();
            
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                lineProcessor.accept(nextLine);
            }
        } catch (CsvException e) {
            throw new IOException("Erreur lors de la lecture du CSV", e);
        }
    }

    public static void writeCSVToFile(String filePath, Stream<String[]> dataStream) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath), ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            dataStream.forEach(writer::writeNext);
        }
    }
}
