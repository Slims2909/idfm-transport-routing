package fr.u_paris.gla.project;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

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
    ligne1.addPath(List.of(a, b, c));

    // Création d'une autre ligne
    StopEntry d = new StopEntry("Place d'Italie", 2.356, 48.831);
    StopEntry e = new StopEntry("Tolbiac", 2.355, 48.829);
    TraceEntry ligne7 = new TraceEntry("7");
    ligne7.addPath(List.of(d, e));

    // Création du réseau
    Reseau reseau = new Reseau(List.of(ligne1, ligne7));

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

}
