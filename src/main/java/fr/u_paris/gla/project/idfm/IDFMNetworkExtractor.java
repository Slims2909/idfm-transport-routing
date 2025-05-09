package fr.u_paris.gla.project.idfm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.u_paris.gla.project.utils.CSVTools;
import fr.u_paris.gla.project.utils.GPS;

/** Code of an extractor for the data from IDF mobilite.
 * 
 * @author Emmanuel Bigeon */
public class IDFMNetworkExtractor {

    /** The logger for information on the process */
    private static final Logger LOGGER = Logger.getLogger(IDFMNetworkExtractor.class.getName());
    
    static {
        // Configuration du logger
        LOGGER.setLevel(Level.INFO);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        // Format simple pour les logs
        SimpleFormatter formatter = new SimpleFormatter();
        handler.setFormatter(formatter);
        LOGGER.addHandler(handler);
        // Pour éviter la duplication des logs
        LOGGER.setUseParentHandlers(false);
    }

    // IDF mobilite API URLs
    private static final String TRACE_FILE_URL = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/traces-des-lignes-de-transport-en-commun-idfm/exports/csv?lang=fr&timezone=Europe%2FBerlin&use_labels=true&delimiter=%3B";
    private static final String STOPS_FILE_URL = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/arrets-lignes/exports/csv?lang=fr&timezone=Europe%2FBerlin&use_labels=true&delimiter=%3B";

    // IDF mobilite csv formats
    private static final int IDFM_TRACE_ID_INDEX    = 0;
    private static final int IDFM_TRACE_SNAME_INDEX = 1;
    private static final int IDFM_TRACE_SHAPE_INDEX = 6;

    private static final int IDFM_STOPS_RID_INDEX  = 0;
    private static final int IDFM_STOPS_NAME_INDEX = 3;
    private static final int IDFM_STOPS_LON_INDEX  = 4;
    private static final int IDFM_STOPS_LAT_INDEX  = 5;

    // Magically chosen values
    /** A number of stops on each line */
    private static final int GUESS_STOPS_BY_LINE = 5;

    // Well named constants
    private static final double QUARTER_KILOMETER = .25;

    /** Main entry point for the extractor of IDF mobilite data into a network as
     * defined by this application.
     * 
     * @param args the arguments (expected one for the destination file) */
    public static void main(String[] args) {
        // Test simple pour vérifier que la classe est appelée
        System.err.println("=== IDFMNetworkExtractor démarré ===");

        if (args.length != 1) {
            System.err.println("Usage: java IDFMNetworkExtractor <output_directory>");
            System.err.println("Arguments reçus : " + java.util.Arrays.toString(args));
            return;
        }

        String outputDir = args[0];
        System.out.println("=== Début de l'extraction des données ===");
        System.out.println("Dossier de sortie : " + outputDir);
        System.out.flush(); // Force l'affichage
        
        try {
            // Créer le répertoire de sortie s'il n'existe pas
            Path outputPath = Path.of(outputDir);
            Files.createDirectories(outputPath);
            if (!Files.isDirectory(outputPath)) {
                throw new IOException("Impossible de créer le dossier : " + outputDir);
            }
            System.out.println("✓ Répertoire créé/vérifié : " + outputDir);
            System.out.flush();

            // Télécharger le fichier des tracés
            System.out.println("\nTéléchargement des tracés...");
            System.out.println("URL : " + TRACE_FILE_URL);
            String tracesFile = outputDir + "/traces.csv";
            downloadFile(TRACE_FILE_URL, tracesFile);
            System.out.println("✓ Fichier des tracés téléchargé : " + tracesFile);
            System.out.println("Taille : " + Files.size(Path.of(tracesFile)) + " octets\n");

            // Télécharger le fichier des arrêts
            System.out.println("Téléchargement des arrêts...");
            System.out.println("URL : " + STOPS_FILE_URL);
            String stopsFile = outputDir + "/stops.csv";
            downloadFile(STOPS_FILE_URL, stopsFile);
            System.out.println("✓ Fichier des arrêts téléchargé : " + stopsFile);
            System.out.println("Taille : " + Files.size(Path.of(stopsFile)) + " octets\n");

            System.out.println("Traitement des données...");
            // Read traces
            System.out.println("Lecture des tracés...");
            Map<String, TraceEntry> traces = new HashMap<>();
            try (InputStream is = Files.newInputStream(Path.of(tracesFile));
                 Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                CSVTools.readCSV(reader, line -> addLine(line, traces));
            }

            // Read stops
            System.out.println("Lecture des arrêts...");
            List<StopEntry> stops = new ArrayList<>();
            try (InputStream is = Files.newInputStream(Path.of(stopsFile));
                 Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                CSVTools.readCSV(reader, line -> addStop(line, traces, stops));
            }

            System.out.println("\nNettoyage des données...");
            cleanTraces(traces);
            System.out.println("✓ Nettoyage terminé");

            System.out.println("\nGénération du réseau...");
            CSVStreamProvider provider = new CSVStreamProvider(traces.values().iterator());
            try {
                String networkFile = outputDir + "/network.csv";
                CSVTools.writeCSVToFile(networkFile, Stream.iterate(provider.next(),
                        t -> provider.hasNext(), t -> provider.next()));
                System.out.println("✓ Réseau généré : " + networkFile);
                System.out.println("Taille : " + Files.size(Path.of(networkFile)) + " octets");
            } catch (IOException e) {
                System.err.println("❌ Erreur lors de la génération du réseau : " + e.getMessage());
                throw e;
            }
        } catch (IOException e) {
            System.err.println("\n❌ ERREUR : " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
        System.out.println("\n=== Extraction terminée avec succès ===");
    }

    private static void cleanTraces(Map<String, TraceEntry> traces) {
        Set<String> toRemove = new HashSet<>();
        for (Entry<String, TraceEntry> traceEntry : traces.entrySet()) {
            TraceEntry trace = traceEntry.getValue();
            if (!cleanLine(trace.getPaths())) {
                LOGGER.severe(() -> MessageFormat.format(
                        "Missing stop for line {0}. Line will be removed", trace.lname));
                toRemove.add(traceEntry.getKey());
            }
        }

        for (String string : toRemove) {
            traces.remove(string);
        }
    }

    /** @param path */
    private static boolean cleanLine(List<List<StopEntry>> stops) {
        for (List<StopEntry> path : stops) {
            for (int i = 0; i < path.size(); i++) {
                StopEntry stop = path.get(i);
                if (!(stop instanceof UnidentifiedStopEntry)) {
                    continue;
                }
                UnidentifiedStopEntry unidentified = (UnidentifiedStopEntry) stop;
                StopEntry stopResolution = unidentified.resolve();
                if (stopResolution == null) {
                    return false;
                }
                path.set(i, stopResolution);
            }
        }
        return true;
    }

    private static void addStop(String[] line, Map<String, TraceEntry> traces,
            List<StopEntry> stops) {
        StopEntry entry = new StopEntry(line[IDFM_STOPS_NAME_INDEX],
                Double.parseDouble(line[IDFM_STOPS_LON_INDEX]),
                Double.parseDouble(line[IDFM_STOPS_LAT_INDEX]));
        String rid = line[IDFM_STOPS_RID_INDEX];
        traces.computeIfPresent(rid,
                (String k, TraceEntry trace) -> addCandidate(trace, entry));
        stops.add(entry);
    }

    private static void addLine(String[] line, Map<String, TraceEntry> traces) {
        TraceEntry entry = new TraceEntry(line[IDFM_TRACE_SNAME_INDEX]);
        List<List<StopEntry>> buildPaths = buildPaths(line[IDFM_TRACE_SHAPE_INDEX]);
        for (List<StopEntry> path : buildPaths) {
            entry.addPath(path);
        }
        if (buildPaths.isEmpty()) {
            LOGGER.severe(() -> MessageFormat.format(
                    "Line {0} has no provided itinerary and was ignored", entry.lname));
        } else {
            traces.put(line[IDFM_TRACE_ID_INDEX], entry);
        }
    }

    private static TraceEntry addCandidate(TraceEntry trace, StopEntry entry) {
        for (List<StopEntry> path : trace.getPaths()) {
            for (StopEntry stopEntry : path) {
                if (stopEntry instanceof UnidentifiedStopEntry unidentified
                        && GPS.distance(entry.latitude, entry.longitude,
                                stopEntry.latitude,
                                stopEntry.longitude) < QUARTER_KILOMETER) {
                    unidentified.addCandidate(entry);
                }
            }
        }
        return trace;
    }

    private static List<List<StopEntry>> buildPaths(String pathsJSON) {
        List<List<StopEntry>> all = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(pathsJSON);
            JSONArray paths = json.getJSONArray("coordinates");
            for (int i = 0; i < paths.length(); i++) {
                JSONArray path = paths.getJSONArray(i);
                List<StopEntry> stopsPath = new ArrayList<>();
                for (int j = 0; j < path.length(); j++) {
                    JSONArray coordinates = path.getJSONArray(j);

                    StopEntry entry = new UnidentifiedStopEntry(coordinates.getDouble(0),
                            coordinates.getDouble(1));

                    stopsPath.add(entry);
                }

                all.add(stopsPath);
            }
        } catch (JSONException e) {
            // Ignoring invalid element!
            LOGGER.log(Level.FINE, e,
                    () -> MessageFormat.format("Invalid json element {0}", pathsJSON)); //$NON-NLS-1$
        }
        return all;
    }

    private static void downloadFile(String urlStr, String outputPath) throws IOException {
        System.out.println("Début du téléchargement depuis : " + urlStr);
        URL url = new URL(urlStr);
        System.out.println("URL créée, ouverture de la connexion...");
        try (InputStream in = url.openStream()) {
            System.out.println("Connexion établie, début de la copie vers : " + outputPath);
            Files.copy(in, Path.of(outputPath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copie terminée avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du téléchargement : " + e.getMessage());
            throw e;
        }
    }
}
