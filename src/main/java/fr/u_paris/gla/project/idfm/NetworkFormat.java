package fr.u_paris.gla.project.idfm;

/**
 * Constantes pour les indices des colonnes dans les fichiers CSV
 */
public class NetworkFormat {
    // Indices des colonnes pour les stations de départ
    public static final int START_STOP_ID_INDEX = 0;
    public static final int START_STOP_NAME_INDEX = 1;
    public static final int START_STOP_LAT_INDEX = 2;
    public static final int START_STOP_LON_INDEX = 3;
    
    // Indices des colonnes pour les stations d'arrivée
    public static final int END_STOP_ID_INDEX = 4;
    public static final int END_STOP_NAME_INDEX = 5;
    public static final int END_STOP_LAT_INDEX = 6;
    public static final int END_STOP_LON_INDEX = 7;
    
    // Autres indices
    public static final int DISTANCE_INDEX = 8;
    public static final int DURATION_INDEX = 9;
    public static final int LINE_INDEX = 10;
}