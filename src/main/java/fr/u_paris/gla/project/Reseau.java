package fr.u_paris.gla.project;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
}
