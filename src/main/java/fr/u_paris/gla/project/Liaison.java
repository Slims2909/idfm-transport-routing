package fr.u_paris.gla.project;

import java.util.Objects;

import fr.u_paris.gla.project.idfm.StopEntry;

/**
 * Représente une liaison directe entre deux arrêts d’une ligne.
 */
public class Liaison {
    private final String nomLigne;
    private final String idLiaison;
    private final StopEntry stationDepart;
    private final StopEntry stationArrivee;
    private final float duree;      // en secondes
    private final float distance;   // en kilomètres

    public Liaison(String nomLigne, String idLiaison, StopEntry stationDepart, StopEntry stationArrivee, float duree, float distance) {
        this.nomLigne = nomLigne;
        this.idLiaison = idLiaison;
        this.stationDepart = stationDepart;
        this.stationArrivee = stationArrivee;
        this.duree = duree;
        this.distance = distance;
    }

    public String getNomLigne() {
        return nomLigne;
    }

    public String getIdLiaison() {
        return idLiaison;
    }

    public StopEntry getStationDepart() {
        return stationDepart;
    }

    public StopEntry getStationArrivee() {
        return stationArrivee;
    }

    public float getDuree() {
        return duree;
    }

    public float getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return String.format(
            "Ligne %s (%s) : %s → %s | %.1f km en %.0f sec",
            nomLigne,
            idLiaison,
            stationDepart.getName(),
            stationArrivee.getName(),
            distance,
            duree
        );
    }

    public static Liaison fromCSVLine(String line) {
        String[] parts = line.split(";");
        if (parts.length == 8) {
            try {
                String nomLigne = parts[0];
                String idLiaison = parts[1];
                String stationDepartName = parts[2];
                String stationArriveeName = parts[4];
                String[] departCoords = parts[3].split(",");
                String[] arriveeCoords = parts[5].split(",");
                float temps = Float.parseFloat(parts[6].replace("\"", "").split(":")[0]) * 60
                            + Float.parseFloat(parts[6].replace("\"", "").split(":")[1]);
                float distance = Float.parseFloat(parts[7].replace("\"", ""));
    
                StopEntry depart = new StopEntry(
                    stationDepartName,
                    Double.parseDouble(departCoords[0].replaceAll("\"", "")),
                    Double.parseDouble(departCoords[1].replaceAll("\"", ""))
                );
    
                StopEntry arrivee = new StopEntry(
                    stationArriveeName,
                    Double.parseDouble(arriveeCoords[0].replaceAll("\"", "")),
                    Double.parseDouble(arriveeCoords[1].replaceAll("\"", ""))
                );
    
                return new Liaison(nomLigne, idLiaison, depart, arrivee, temps, distance);
    
            } catch (Exception e) {
                System.out.println("Erreur lors du parsing : " + line);
            }
        } else {
            System.out.println("Format de ligne incorrect : " + line);
        }
        return null;
    }

        @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Liaison liaison = (Liaison) o;
        return nomLigne.equals(liaison.nomLigne) && idLiaison.equals(liaison.idLiaison);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nomLigne, idLiaison);
    }

}
