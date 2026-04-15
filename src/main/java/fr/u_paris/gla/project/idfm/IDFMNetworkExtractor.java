package fr.u_paris.gla.project.idfm;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.u_paris.gla.project.utils.CSVTools;
import fr.u_paris.gla.project.utils.GPS;

public class IDFMNetworkExtractor {
    private static final Logger LOGGER = Logger.getLogger(IDFMNetworkExtractor.class.getName());

    // URLs de l'API IDFM
    private static final String TRACE_FILE_URL = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/traces-des-lignes-de-transport-en-commun-idfm/exports/csv?lang=fr&timezone=Europe%2FBerlin&use_labels=true&delimiter=%3B";
    private static final String STOPS_FILE_URL = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/arrets-lignes/exports/csv?lang=fr&timezone=Europe%2FBerlin&use_labels=true&delimiter=%3B";

    // Indices des colonnes dans les fichiers CSV
    private static final int IDFM_TRACE_ID_INDEX = 0;
    private static final int IDFM_TRACE_SNAME_INDEX = 1;
    private static final int IDFM_TRACE_SHAPE_INDEX = 2;

    private static final int IDFM_STOPS_RID_INDEX = 0;
    private static final int IDFM_STOPS_NAME_INDEX = 1;
    private static final int IDFM_STOPS_LON_INDEX = 2;
    private static final int IDFM_STOPS_LAT_INDEX = 3;

    private static final double QUARTER_KILOMETER = 0.25;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: IDFMNetworkExtractor <output-file.csv>");
            System.out.println("Example: IDFMNetworkExtractor data/network.csv");
            return;
        }

        String outputFile = args[0];
        System.out.println("Début du traitement...");
        
        // Vérifier que le chemin de sortie se termine par .csv
        if (!outputFile.toLowerCase().endsWith(".csv")) {
            System.out.println("Le fichier de sortie doit avoir l'extension .csv");
            return;
        }
        
        // Créer le dossier parent du fichier de sortie s'il n'existe pas
        File output = new File(outputFile);
        File parentDir = output.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }
        
        String tracesFile = "data/traces.csv";
        String stopsFile = "data/stops.csv";

        // Créer le dossier data s'il n'existe pas
        new File("data").mkdirs();

        // Télécharger les traces des lignes
        try {
            System.out.println("Téléchargement des traces des lignes...");
            CSVTools.downloadCSVFromURL(TRACE_FILE_URL, tracesFile);
            System.out.println("Traces téléchargées avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du téléchargement des traces : " + e.getMessage());
            return;
        }

        // Télécharger les arrêts
        try {
            System.out.println("Téléchargement des arrêts...");
            CSVTools.downloadCSVFromURL(STOPS_FILE_URL, stopsFile);
            System.out.println("Arrêts téléchargés avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du téléchargement des arrêts : " + e.getMessage());
            return;
        }

        // Formater les données dans le format requis
        try {
            System.out.println("Formatage des données...");
            Path networkPath = Paths.get(outputFile);
            Path schedulesPath = Paths.get("data/schedules.csv");
            CSVFormatter.formatNetworkData(Paths.get(tracesFile), Paths.get(stopsFile), networkPath);
            System.out.println("Données du réseau formatées avec succès dans : " + outputFile);

            // Générer les horaires
            System.out.println("Génération des horaires...");
            ScheduleGenerator.generateSchedules(networkPath, schedulesPath);
            System.out.println("Horaires générés avec succès dans : " + schedulesPath);
            System.out.println("Traitement terminé !");
        } catch (IOException e) {
            System.err.println("Erreur lors du formatage des données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void cleanTraces(Map<String, TraceEntry> traces) {
        Set<String> toRemove = new HashSet<>();
        for (Entry<String, TraceEntry> entry : traces.entrySet()) {
            TraceEntry trace = entry.getValue();
            if (!cleanLine(trace.getPaths())) {
                LOGGER.warning("Ligne supprimée car arrêts manquants : " + trace.lname);
                toRemove.add(entry.getKey());
            }
        }
        toRemove.forEach(traces::remove);
    }

    private static boolean cleanLine(List<List<StopEntry>> paths) {
        for (List<StopEntry> path : paths) {
            for (int i = 0; i < path.size(); i++) {
                StopEntry stop = path.get(i);
                if (stop instanceof UnidentifiedStopEntry) {
                    UnidentifiedStopEntry unidentified = (UnidentifiedStopEntry) stop;
                    StopEntry resolved = unidentified.resolve();
                    if (resolved == null) {
                        return false;
                    }
                    path.set(i, resolved);
                }
            }
        }
        return true;
    }

    private static void addStop(String[] line, Map<String, TraceEntry> traces, List<StopEntry> stops) {
        try {
            StopEntry entry = new StopEntry(
                line[IDFM_STOPS_NAME_INDEX],
                Double.parseDouble(line[IDFM_STOPS_LON_INDEX]),
                Double.parseDouble(line[IDFM_STOPS_LAT_INDEX])
            );
            String rid = line[IDFM_STOPS_RID_INDEX];
            traces.computeIfPresent(rid, (k, trace) -> addCandidate(trace, entry));
            stops.add(entry);
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de l'ajout de l'arrêt : " + e.getMessage());
        }
    }

    private static void addLine(String[] line, Map<String, TraceEntry> traces) {
        try {
            TraceEntry entry = new TraceEntry(line[IDFM_TRACE_SNAME_INDEX]);
            List<List<StopEntry>> paths = buildPaths(line[IDFM_TRACE_SHAPE_INDEX]);
            entry.getPaths().addAll(paths);
            if (paths.isEmpty()) {
                LOGGER.warning("Ligne ignorée car pas d'itinéraire : " + entry.lname);
            } else {
                traces.put(line[IDFM_TRACE_ID_INDEX], entry);
            }
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de l'ajout de la ligne : " + e.getMessage());
        }
    }

    private static TraceEntry addCandidate(TraceEntry trace, StopEntry entry) {
        for (List<StopEntry> path : trace.getPaths()) {
            for (StopEntry stop : path) {
                if (stop instanceof UnidentifiedStopEntry) {
                    UnidentifiedStopEntry unidentified = (UnidentifiedStopEntry) stop;
                    if (GPS.distance(entry.latitude, entry.longitude,
                            stop.latitude, stop.longitude) < QUARTER_KILOMETER) {
                        unidentified.addCandidate(entry);
                    }
                }
            }
        }
        return trace;
    }

    private static List<List<StopEntry>> buildPaths(String pathsJSON) {
        List<List<StopEntry>> paths = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(pathsJSON);
            JSONArray coordinates = json.getJSONArray("coordinates");
            for (int i = 0; i < coordinates.length(); i++) {
                JSONArray path = coordinates.getJSONArray(i);
                List<StopEntry> stopsPath = new ArrayList<>();
                for (int j = 0; j < path.length(); j++) {
                    JSONArray point = path.getJSONArray(j);
                    stopsPath.add(new UnidentifiedStopEntry(
                        point.getDouble(0), point.getDouble(1)));
                }
                paths.add(stopsPath);
            }
        } catch (JSONException e) {
            LOGGER.fine("JSON invalide ignoré : " + e.getMessage());
        }
        return paths;
    }
}
