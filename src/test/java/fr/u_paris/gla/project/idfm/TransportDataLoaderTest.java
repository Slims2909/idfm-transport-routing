package fr.u_paris.gla.project.idfm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TransportDataLoaderTest {
    
    @TempDir
    Path tempDir;
    
    private TransportDataLoader loader;
    private GrapheTransport graphe;
    private File stopsFile;
    private File routesFile;
    private File segmentsFile;
    
    @BeforeEach
    void setUp() throws IOException {
        graphe = new GrapheTransport();
        loader = new TransportDataLoader(graphe);
        
        // Créer les fichiers CSV de test
        stopsFile = createStopsFile();
        routesFile = createRoutesFile();
        segmentsFile = createSegmentsFile();
    }
    
    private File createStopsFile() throws IOException {
        File file = tempDir.resolve("stops.csv").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("stop_id,stop_name,stop_lat,stop_lon,location_type\n");
            writer.write("S1,Châtelet,48.8586,2.3488,1\n");
            writer.write("S2,Gare du Nord,48.8809,2.3553,1\n");
            writer.write("S3,Nation,48.8483,2.3962,1\n");
        }
        return file;
    }
    
    private File createRoutesFile() throws IOException {
        File file = tempDir.resolve("routes.csv").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("route_id,route_long_name,route_type,route_color\n");
            writer.write("L1,Ligne 1,1,FFCD00\n");
            writer.write("L4,Ligne 4,1,BB4097\n");
            writer.write("L5,Ligne 5,1,FF7E2E\n");
        }
        return file;
    }
    
    private File createSegmentsFile() throws IOException {
        File file = tempDir.resolve("segments.csv").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("route_id,from_stop_id,to_stop_id,duration,distance\n");
            writer.write("L4,S1,S2,300,2500\n");
            writer.write("L5,S2,S3,420,3500\n");
            writer.write("L1,S1,S3,600,5000\n");
        }
        return file;
    }
    
    @Test
    void testLoadTransportData() throws IOException {
        // Charger les données
        loader.loadStops(stopsFile.getAbsolutePath());
        loader.loadLignes(routesFile.getAbsolutePath());
        loader.loadSegments(segmentsFile.getAbsolutePath());
        
        // Vérifier les stations
        Set<Station> stations = graphe.getStations();
        assertEquals(3, stations.size());
        
        Station chatelet = graphe.getStation("S1");
        assertNotNull(chatelet);
        assertEquals("Châtelet", chatelet.getName());
        assertEquals(48.8586, chatelet.getLatitude(), 0.0001);
        assertEquals(2.3488, chatelet.getLongitude(), 0.0001);
        
        // Vérifier les lignes
        Map<String, Ligne> lignes = loader.getLignes();
        assertEquals(3, lignes.size());
        
        Ligne ligne4 = lignes.get("L4");
        assertNotNull(ligne4);
        assertEquals("Ligne 4", ligne4.getName());
        assertEquals("BB4097", ligne4.getColor());
        
        // Vérifier les segments
        assertEquals(1, ligne4.getSegments().size());
        
        Segment segment = ligne4.getSegments().get(0);
        assertEquals(chatelet, segment.getStation1());
        assertEquals("S2", segment.getStation2().getId());
        assertEquals(300, segment.getDuration());
        assertEquals(2500, segment.getDistance());
        
        // Vérifier la connexité
        Ligne ligne5 = lignes.get("L5");
        Ligne ligne1 = lignes.get("L1");
        
        assertTrue(ligne5.getSegments().stream()
            .anyMatch(s -> s.getStation1().getId().equals("S2") && 
                         s.getStation2().getId().equals("S3")));
                         
        assertTrue(ligne1.getSegments().stream()
            .anyMatch(s -> s.getStation1().getId().equals("S1") && 
                         s.getStation2().getId().equals("S3")));
    }
    
    @Test
    void testLoadInvalidData() {
        // Vérifier que le chargement de fichiers inexistants lève une exception
        assertThrows(IOException.class, () -> 
            loader.loadStops("invalid_file.csv"));
            
        // Vérifier que les segments avec des stops invalides sont ignorés
        assertDoesNotThrow(() -> {
            loader.loadStops(stopsFile.getAbsolutePath());
            loader.loadLignes(routesFile.getAbsolutePath());
            
            // Créer un fichier de segments avec un stop invalide
            File invalidSegments = tempDir.resolve("invalid_segments.csv").toFile();
            try (FileWriter writer = new FileWriter(invalidSegments)) {
                writer.write("route_id,from_stop_id,to_stop_id,duration,distance\n");
                writer.write("L4,INVALID,S2,300,2500\n");
            }
            
            loader.loadSegments(invalidSegments.getAbsolutePath());
        });
        
        // Vérifier qu'aucun segment invalide n'a été chargé
        Map<String, Ligne> lignes = loader.getLignes();
        assertEquals(0, lignes.get("L4").getSegments().size());
    }
}
