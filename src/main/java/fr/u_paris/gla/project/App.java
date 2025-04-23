package fr.u_paris.gla.project;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Arrays;

import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import fr.u_paris.gla.project.idfm.CSVStreamProvider;
import fr.u_paris.gla.project.idfm.StopEntry;
import fr.u_paris.gla.project.idfm.TraceEntry;

/** Simple application model.
 *
 * @author Emmanuel Bigeon */
public class App {
    /**
     * 
     */
    private static final String UNSPECIFIED = "Unspecified";         //$NON-NLS-1$
    /** The logo image name. */
    private static final String LOGO_NAME   = "uparis_logo_rvb.png"; //$NON-NLS-1$
    /** Image height. */
    private static final int    HEIGHT      = 256;
    /** Image width. */
    private static final int    WIDTH       = HEIGHT;

    /** Resizes an image.
     *
     * @param src source image
     * @param w width
     * @param h height
     * @return the resized image */
    private static Image getScaledImage(Image src, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(src, 0, 0, w, h, null);
        g2d.dispose();
        return resizedImg;
    }

    /** Application entry point.
     *
     * @param args launching arguments */
    public static void main(String[] args) {
        if (args.length > 0) {
            for (String string : args) {
                if ("--info".equals(string)) { //$NON-NLS-1$
                    printAppInfos(System.out);
                    return;
                }
                if ("--gui".equals(string)) { //$NON-NLS-1$
                    showLogo();
                }

                if ("--test-reseau".equals(string)) {
                    testReseau();
                    return;
                }

                if ("--test-liaisons".equals(string)) {
                    testLiaisons();
                    return;
                }

                if ("--generate-csv".equals(string)) {
                    generateCSVFile();
                    return;
                }
                
                
            }
        }
    }

    /** @param out */
    public static void printAppInfos(PrintStream out) {
        Properties props = new Properties();
        try (InputStream is = App.class.getResourceAsStream("application.properties")) { //$NON-NLS-1$
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read application informations", e); //$NON-NLS-1$
        }

        out.println("Application: " + props.getProperty("app.name", UNSPECIFIED)); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("Version: " + props.getProperty("app.version", UNSPECIFIED)); //$NON-NLS-1$ //$NON-NLS-2$
        out.println("By: " + props.getProperty("app.team", UNSPECIFIED)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /** Shows the logo in an image. */
    public static void showLogo() {
        Properties props = new Properties();
        try (InputStream is = App.class.getResourceAsStream("application.properties")) { //$NON-NLS-1$
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read application informations", e); //$NON-NLS-1$
        }

        JFrame frame = new JFrame(props.getProperty("app.name")); //$NON-NLS-1$
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JLabel container = new JLabel();

        try (InputStream is = App.class.getResourceAsStream(LOGO_NAME)) {
            if (is == null) {
                container.setText("Image Not Found");
            } else {
                BufferedImage img = ImageIO.read(is);
                ImageIcon icon = new ImageIcon(img);
                ImageIcon resized = new ImageIcon(
                        getScaledImage(icon.getImage(), WIDTH, HEIGHT));

                container.setIcon(resized);
            }
        } catch (IOException e) {
            container.setText("Image Not Read: " + e.getLocalizedMessage());
        }

        frame.getContentPane().add(container);

        frame.pack();
        frame.setVisible(true);
    }

    public static void testReseau() {
    // Création de quelques arrêts
        StopEntry a = new StopEntry("Nation", 2.395, 48.848);
        StopEntry b = new StopEntry("Reuilly-Diderot", 2.390, 48.846);
        StopEntry c = new StopEntry("Gare de Lyon", 2.373, 48.844);

        // Création de la ligne 1
        TraceEntry ligne1 = new TraceEntry("1");
        ligne1.addPath(Arrays.asList(a, b, c));

        // Création d'une autre ligne
        StopEntry d = new StopEntry("Place d'Italie", 2.356, 48.831);
        StopEntry e = new StopEntry("Tolbiac", 2.355, 48.829);
        TraceEntry ligne7 = new TraceEntry("7");
        ligne7.addPath(Arrays.asList(d, e));

        // Création du réseau
        Reseau reseau = new Reseau(Arrays.asList(ligne1, ligne7));

        // Affichage
        System.out.println("Réseau contient " + reseau.getNombreDeLignes() + " ligne(s).");

        for (TraceEntry ligne : reseau.getToutesLesLignes()) {
            System.out.println("Ligne : " + ligne.getName());
            for (List<StopEntry> chemin : ligne.getPaths()) {
                for (StopEntry stop : chemin) {
                    System.out.println(" - " + stop.getName());
                }
            }
        }
    } 


    public static void testLiaisons() {
        String fichier = "src/data/reseau.csv"; 
    
        List<Liaison> liaisons = Reseau.createLiaisonsFromCSV(fichier);
        System.out.println("Nombre de liaisons chargées : " + liaisons.size());
    
        for (Liaison liaison : liaisons) {
            System.out.println(liaison);
        }
    
        // Conversion en lignes
        List<TraceEntry> traces = Reseau.getTracesFromLiaisons(liaisons);
        System.out.println("\nNombre de lignes détectées : " + traces.size());
    
        for (TraceEntry ligne : traces) {
            System.out.println("Ligne " + ligne.getName() + " : " + ligne.getNumberOfPaths() + " trajets");
        }
    
        // Création du réseau complet
        Reseau reseau = new Reseau(traces);
        System.out.println("\nRéseau contient " + reseau.getNombreDeLignes() + " ligne(s).");
    }
    
    public static void exportCSVFromTraces(List<TraceEntry> traces, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            CSVStreamProvider provider = new CSVStreamProvider(traces.iterator());
    
            // Écrire l’en-tête si besoin
            writer.write("ligne;station_depart;coord_depart;station_arrivee;coord_arrivee;temps;distance;variante\n");
    
            while (provider.hasNext()) {
                String[] ligne = provider.next();
                writer.write(String.join(";", ligne) + "\n");
            }
    
            System.out.println("Fichier CSV généré avec succès : " + filename);
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void generateCSVFile() {
        System.out.println(" Génération du fichier CSV...");
    
        // Simule quelques liaisons pour créer un fichier de test
        Liaison l1 = new Liaison("1", "LIA001",
            new StopEntry("Nation", 2.395, 48.848),
            new StopEntry("Reuilly-Diderot", 2.390, 48.846),
            90, 1.2f
        );
    
        Liaison l2 = new Liaison("1", "LIA002",
            new StopEntry("Reuilly-Diderot", 2.390, 48.846),
            new StopEntry("Gare de Lyon", 2.373, 48.844),
            100, 1.5f
        );
    
        Liaison l3 = new Liaison("7", "LIA003",
            new StopEntry("Place d'Italie", 2.356, 48.831),
            new StopEntry("Tolbiac", 2.355, 48.829),
            80, 1.0f
        );
    
        List<Liaison> liaisons = Arrays.asList(l1, l2, l3);
        List<TraceEntry> traces = Reseau.getTracesFromLiaisons(liaisons);
    
        exportCSVFromTraces(traces, "src/data/reseau.csv");
    }
    
}
