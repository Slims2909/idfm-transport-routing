package fr.u_paris.gla.project.idfm;

import fr.u_paris.gla.project.idfm.graph.Node;
import fr.u_paris.gla.project.idfm.graph.PathFinder;
import fr.u_paris.gla.project.idfm.StopInfo;
import fr.u_paris.gla.project.idfm.Schedule;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.geometry.Pos;

public class RouteFinderController {
    private static final String NETWORK_FILE = "data/network.csv";
    private static final String SCHEDULE_FILE = "data/schedules.csv";
    
    @FXML private ComboBox<String> fromStation;
    @FXML private ComboBox<String> toStation;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> hourPicker;
    @FXML private ComboBox<String> minutePicker;
    @FXML private ImageView planView;
    @FXML private Label durationLabel;
    @FXML private Label stopsLabel;
    @FXML private Label departureTimeLabel;
    @FXML private Label arrivalTimeLabel;
    @FXML private VBox stepsContainer;
    
    private PathFinder finder;
    private Set<String> stations;
    private Map<String, Color> lineColors;
    
    @FXML
    public void initialize() {
        try {
            initializeTimeControls();
            loadData();
            setupComboBoxes();
            initializeLineColors();
        } catch (IOException e) {
            showError("Erreur lors du chargement des données : " + e.getMessage());
        }
    }
    
    private void loadData() throws IOException {
        // Charger le réseau
        Map<String, List<StopInfo>> lines = NetworkReader.readNetwork(Paths.get(NETWORK_FILE));
        
        // Créer le PathFinder
        finder = new PathFinder();
        stations = new TreeSet<>(); // Utiliser TreeSet pour trier automatiquement
        
        // Charger les horaires
        List<Schedule> schedules = ScheduleReader.readSchedules(Paths.get(SCHEDULE_FILE));
        
        // Ajouter les lignes au PathFinder et collecter les stations
        for (Map.Entry<String, List<StopInfo>> entry : lines.entrySet()) {
            String lineId = entry.getKey();
            List<StopInfo> stops = entry.getValue();
            
            // Collecter toutes les stations pour les ComboBox
            for (StopInfo stop : stops) {
                String stationName = cleanStationName(stop.getName());
                if (!stationName.isEmpty()) {
                    stations.add(stationName);
                }
            }
            
            // Calculer les durées et ajouter la ligne
            List<Integer> durations = calculateDurations(stops, schedules, lineId);
            finder.addLine(lineId, stops, durations);
        }
    }
    
    private void setupComboBoxes() {
        // Configurer les ComboBox avec autocomplétion
        fromStation.setItems(FXCollections.observableArrayList(stations));
        toStation.setItems(FXCollections.observableArrayList(stations));
        
        // Ajouter l'autocomplétion
        fromStation.setEditable(true);
        toStation.setEditable(true);
        
        // Filtrer les suggestions pendant la saisie
        fromStation.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                fromStation.setItems(FXCollections.observableArrayList(stations));
            } else {
                String finalText = newText.toLowerCase();
                fromStation.setItems(FXCollections.observableArrayList(
                    stations.stream()
                           .filter(station -> station.toLowerCase().contains(finalText))
                           .sorted()
                           .collect(Collectors.toList())
                ));
                fromStation.show();
            }
        });
        
        toStation.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                toStation.setItems(FXCollections.observableArrayList(stations));
            } else {
                String finalText = newText.toLowerCase();
                toStation.setItems(FXCollections.observableArrayList(
                    stations.stream()
                           .filter(station -> station.toLowerCase().contains(finalText))
                           .sorted()
                           .collect(Collectors.toList())
                ));
                toStation.show();
            }
        });
    }
    
    private void initializeTimeControls() {
        // Initialiser la date à aujourd'hui
        datePicker.setValue(LocalDate.now());
        
        // Initialiser les heures (00-23)
        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        hourPicker.setItems(FXCollections.observableArrayList(hours));
        
        // Initialiser les minutes (00-59)
        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutes.add(String.format("%02d", i));
        }
        minutePicker.setItems(FXCollections.observableArrayList(minutes));
        
        // Définir l'heure actuelle comme valeur par défaut
        LocalTime now = LocalTime.now();
        hourPicker.setValue(String.format("%02d", now.getHour()));
        minutePicker.setValue(String.format("%02d", now.getMinute()));
    }
    
    private void initializeLineColors() {
        lineColors = new HashMap<>();
        lineColors.put("1", Color.YELLOW);
        lineColors.put("2", Color.BLUE);
        lineColors.put("3", Color.GREEN);
        lineColors.put("3bis", Color.LIGHTBLUE);
        lineColors.put("4", Color.PURPLE);
        lineColors.put("5", Color.ORANGE);
        lineColors.put("6", Color.LIGHTGREEN);
        lineColors.put("7", Color.PINK);
        lineColors.put("7bis", Color.LIGHTPINK);
        lineColors.put("8", Color.DARKVIOLET);
        lineColors.put("9", Color.CHARTREUSE);
        lineColors.put("10", Color.YELLOW);
        lineColors.put("11", Color.BROWN);
        lineColors.put("12", Color.GREEN);
        lineColors.put("13", Color.LIGHTBLUE);
        lineColors.put("14", Color.PURPLE);
    }
    
    private String cleanStationName(String name) {
        // Supprimer les mentions de gare/station si présentes
        name = name.replaceAll("(?i)\\s*-\\s*Gare\\s+de\\s+", " - ");
        name = name.replaceAll("(?i)\\s*-\\s*Station\\s+", " - ");
        
        // Supprimer les espaces multiples
        name = name.replaceAll("\\s+", " ").trim();
        
        // Capitaliser correctement le nom
        String[] parts = name.split("\\s+");
        StringBuilder cleaned = new StringBuilder();
        
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (cleaned.length() > 0) {
                    cleaned.append(" ");
                }
                // Capitaliser la première lettre sauf pour certains mots
                if (part.matches("(?i)^(de|du|des|le|la|les|sur|sous|en|à|au|aux)$")) {
                    cleaned.append(part.toLowerCase());
                } else {
                    cleaned.append(Character.toUpperCase(part.charAt(0)))
                           .append(part.substring(1).toLowerCase());
                }
            }
        }
        
        return cleaned.toString();
    }
    
    private List<Integer> calculateDurations(List<StopInfo> stops, List<Schedule> schedules, String lineId) {
        List<Integer> durations = new ArrayList<>();
        
        // On utilise une durée fixe par défaut entre les stations
        for (int i = 0; i < stops.size() - 1; i++) {
            // Par défaut, on utilise une durée fixe de 5 minutes entre les stations
            durations.add(5);
        }
        
        // Pour chaque horaire de la ligne
        for (Schedule schedule : schedules) {
            if (schedule.getLineId().equals(lineId)) {
                List<Integer> junctions = schedule.getJunctions();
                if (!junctions.isEmpty()) {
                    // Ajouter le terminus de départ au début
                    List<Integer> allStops = new ArrayList<>();
                    allStops.add(0);
                    allStops.addAll(junctions);
                    
                    // Pour chaque paire de jonctions consécutives
                    for (int i = 0; i < allStops.size() - 1; i++) {
                        int start = allStops.get(i);
                        int end = allStops.get(i + 1);
                        
                        // Calculer la durée totale entre ces deux points
                        int totalDuration = 5 * Math.abs(end - start);
                        
                        // Répartir cette durée entre les stations intermédiaires
                        int minIdx = Math.min(start, end);
                        int maxIdx = Math.max(start, end);
                        int segmentLength = maxIdx - minIdx;
                        
                        if (segmentLength > 0) {
                            int durationPerSegment = totalDuration / segmentLength;
                            for (int j = minIdx; j < maxIdx; j++) {
                                durations.set(j, durationPerSegment);
                            }
                        }
                    }
                }
            }
        }
        
        return durations;
    }
    
    @FXML
    private void findPath() {
        String from = fromStation.getValue();
        String to = toStation.getValue();
        
        if (from == null || to == null || from.isEmpty() || to.isEmpty()) {
            showError("Veuillez sélectionner les stations de départ et d'arrivée.");
            return;
        }
        
        // Récupérer l'heure de départ pour l'affichage
        LocalTime departureTime = LocalTime.of(
            Integer.parseInt(hourPicker.getValue()),
            Integer.parseInt(minutePicker.getValue())
        );
        
        List<Node> path = finder.findShortestPath(from, to);
        if (path != null && !path.isEmpty()) {
            displayResult(path, departureTime);
        } else {
            showError("Aucun chemin trouvé.");
        }
    }
    
    private void displayResult(List<Node> path, LocalTime departureTime) {
        // Calculer la durée totale et l'heure d'arrivée
        int duration = 0;
        LocalTime currentTime = departureTime;
        
        // Afficher le nombre d'arrêts
        stopsLabel.setText(String.format("Arrêts : %d", path.size()));
        
        // Afficher l'heure de départ
        departureTimeLabel.setText("Départ : " + departureTime.toString());
        
        // Afficher les étapes
        stepsContainer.getChildren().clear();
        String currentLine = null;
        
        for (int i = 0; i < path.size(); i++) {
            Node node = path.get(i);
            HBox stepBox = new HBox(10);
            stepBox.getStyleClass().add("step");
            
            // Indicateur de ligne et correspondance
            if (!node.getLineId().equals(currentLine)) {
                // Nouvelle ligne
                currentLine = node.getLineId();
                
                // Ajouter l'indicateur de ligne
                HBox lineBox = new HBox(5);
                lineBox.setAlignment(Pos.CENTER_LEFT);
                
                Circle lineIndicator = new Circle(8);
                lineIndicator.setFill(lineColors.getOrDefault(currentLine, Color.GRAY));
                
                Label lineLabel = new Label(currentLine);
                lineLabel.getStyleClass().add("line-number");
                
                lineBox.getChildren().addAll(lineIndicator, lineLabel);
                stepBox.getChildren().add(lineBox);
                
                // Si ce n'est pas le premier arrêt, c'est une correspondance
                if (i > 0) {
                    Label transferLabel = new Label("Correspondance");
                    transferLabel.getStyleClass().add("transfer");
                    VBox transferBox = new VBox(5);
                    transferBox.getChildren().addAll(new Label("↓"), transferLabel);
                    transferBox.setAlignment(Pos.CENTER);
                    stepsContainer.getChildren().add(transferBox);
                }
            }
            
            // Nom de la station
            VBox stationBox = new VBox(2);
            Label stationLabel = new Label(cleanStationName(node.getStop().getName()));
            stationLabel.getStyleClass().add("station-name");
            
            // Ajouter le temps si ce n'est pas le dernier arrêt
            if (i < path.size() - 1) {
                int nextDuration = node.getNeighbors().get(path.get(i + 1));
                duration += nextDuration; // Mettre à jour la durée totale
                currentTime = currentTime.plusMinutes(nextDuration);
                Label timeLabel = new Label(nextDuration + " min");
                timeLabel.getStyleClass().add("duration-label");
                stationBox.getChildren().addAll(stationLabel, timeLabel);
            } else {
                stationBox.getChildren().add(stationLabel);
            }
            
            stepBox.getChildren().add(stationBox);
            stepsContainer.getChildren().add(stepBox);
        }
        
        // Mettre à jour la durée totale et l'heure d'arrivée après avoir tout calculé
        durationLabel.setText(String.format("Durée : %d minutes", duration));
        arrivalTimeLabel.setText("Arrivée : " + currentTime.toString());
    }
    
    private void showError(String message) {
        // Afficher un message d'erreur
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void goHome() {
        // Réinitialiser les champs
        fromStation.setValue(null);
        toStation.setValue(null);
        datePicker.setValue(LocalDate.now());
        hourPicker.setValue(String.format("%02d", LocalTime.now().getHour()));
        minutePicker.setValue(String.format("%02d", LocalTime.now().getMinute()));
        
        // Effacer les résultats
        durationLabel.setText("");
        stopsLabel.setText("");
        departureTimeLabel.setText("");
        arrivalTimeLabel.setText("");
        stepsContainer.getChildren().clear();
    }
    
    @FXML
    private void showMap() {
        // Afficher le plan en plein écran dans une nouvelle fenêtre
        Stage mapStage = new Stage();
        mapStage.setTitle("Plan du réseau");
        
        ImageView fullMapView = new ImageView(planView.getImage());
        fullMapView.setPreserveRatio(true);
        
        ScrollPane scrollPane = new ScrollPane(fullMapView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        Scene scene = new Scene(scrollPane);
        mapStage.setScene(scene);
        mapStage.show();
    }
}
