package fr.u_paris.gla.project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

//import fr.u_paris.gla.project.idfm.StopEntry;
import fr.u_paris.gla.project.idfm.TraceEntry;

public class Reseau {

    // Clé : nom de ligne (ex: "13") ; Valeur : TraceEntry correspondant
    private final Map<String, TraceEntry> lignes = new HashMap<>();

    /**
     * Constructeur d’un réseau à partir d’une collection de lignes
     * @param traceEntries les lignes de transport
     */
    public Reseau(Collection<TraceEntry> traceEntries) {
        for (TraceEntry trace : traceEntries) {
            lignes.put(trace.getName(), trace);
        }
    }
    // si on veut instancier un Reseau sans données au départ.
    public Reseau() {
    }
    

    /**
     * Récupère une ligne par son nom
     * @param nomLigne nom de la ligne (ex : "7", "RER A")
     * @return la TraceEntry correspondante ou null si non trouvée
     */
    public TraceEntry getLigne(String nomLigne) {
        return lignes.get(nomLigne);
    }

    /**
     * Vérifie si une ligne est présente dans le réseau
     * @param nomLigne nom de la ligne
     * @return true si présente, false sinon
     */
    public boolean contient(String nomLigne) {
        return lignes.containsKey(nomLigne);
    }

    /**
     * Récupère toutes les lignes du réseau
     * @return une collection de TraceEntry
     */
    public Collection<TraceEntry> getToutesLesLignes() {
        return lignes.values();
    }

    /**
     * Nombre total de lignes dans le réseau
     * @return nombre de lignes
     */
    public int getNombreDeLignes() {
        return lignes.size();
    }

        //  Lire un fichier CSV ligne par ligne
    public static List<String> readLinesFromCSV(String filename) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static List<Liaison> createLiaisonsFromCSV(String filename) {
        List<Liaison> liaisons = new ArrayList<>();
        List<String> lines = readLinesFromCSV(filename);
        for (String line : lines) {
            Liaison liaison = Liaison.fromCSVLine(line);
            if (liaison != null) {
                liaisons.add(liaison);
            }
        }
        return liaisons;
    }

    public static List<TraceEntry> getTracesFromLiaisons(List<Liaison> liaisons) {
        Map<String, TraceEntry> tracesMap = new HashMap<>();
    
        for (Liaison liaison : liaisons) {
            String ligne = liaison.getNomLigne();
            tracesMap.putIfAbsent(ligne, new TraceEntry(ligne));
            // On ajoute le trajet (station A → B) comme un path de 2 arrêts
            tracesMap.get(ligne).addPath(Arrays.asList(liaison.getStationDepart(), liaison.getStationArrivee()));
        }
    
        return new ArrayList<>(tracesMap.values());
    }

        @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Réseau IDFM :\n");
        for (TraceEntry trace : lignes.values()) {
            sb.append("- Ligne ").append(trace.getName()).append(" avec ").append(trace.getPaths().size()).append(" chemin(s)\n");
        }
        return sb.toString();
    }

}
