package com.nissrine.itineraire;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.Properties;

public class MainFX extends Application {

    private BorderPane root;
    private VBox pageAccueil;
    private VBox pageTrajet;
    private VBox pagePlans;
    private VBox pageHoraires;
    private BorderPane pageCarte;
    private VBox pageAdresse;

    private static String adresseMaison = "";
    private static String adresseTravail = "";
    private String typeAdresse = "";
    private final String FICHIER_ADRESSES = "src/main/resources/adresses.txt";

    @Override
    public void start(Stage primaryStage) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #E5F5F9;");

        chargerAdresses();

        creerPageAccueil();
        creerPageTrajet();
        creerPagePlans();
        creerPageHoraires();
        creerPageCarte();

        root.setCenter(pageAccueil);
        animerTransition(pageAccueil);
        root.setBottom(creerMenuBas());

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("Bonjour Mobilité");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void creerPageAccueil() {
        Label titre = new Label("\uD83D\uDE8C Bonjour Mobilité");
        titre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00788C;");

        Button boutonTrajet = new Button("Planifier un trajet");
        styliserBouton(boutonTrajet);
        boutonTrajet.setOnAction(e -> {
            root.setCenter(pageTrajet);
            animerTransition(pageTrajet);
        });

        ImageView maisonIcon = new ImageView(new Image("file:src/main/resources/icons/home.png"));
        maisonIcon.setFitWidth(32);
        maisonIcon.setFitHeight(32);
        Label labelMaison = new Label("Maison");
        VBox blocMaison = new VBox(5, maisonIcon, labelMaison);
        blocMaison.setAlignment(Pos.CENTER);

        Button addMaison = new Button("+");
        styliserPetitBouton(addMaison);
        addMaison.setOnAction(e -> {
            typeAdresse = "Maison";
            afficherPageAdresse();
        });
        VBox maisonBox = new VBox(5, blocMaison, addMaison);
        maisonBox.setAlignment(Pos.CENTER);

        ImageView travailIcon = new ImageView(new Image("file:src/main/resources/icons/timer.png"));
        travailIcon.setFitWidth(32);
        travailIcon.setFitHeight(32);
        Label labelTravail = new Label("Travail");
        VBox blocTravail = new VBox(5, travailIcon, labelTravail);
        blocTravail.setAlignment(Pos.CENTER);

        Button addTravail = new Button("+");
        styliserPetitBouton(addTravail);
        addTravail.setOnAction(e -> {
            typeAdresse = "Travail";
            afficherPageAdresse();
        });
        VBox travailBox = new VBox(5, blocTravail, addTravail);
        travailBox.setAlignment(Pos.CENTER);

        HBox raccourcis = new HBox(30, maisonBox, travailBox);
        raccourcis.setAlignment(Pos.CENTER);

        pageAccueil = new VBox(20, titre, boutonTrajet, raccourcis);
        pageAccueil.setAlignment(Pos.CENTER);
    }

    private void creerPageTrajet() {
        Label titre = new Label("Planification de Trajet");
        titre.setStyle("-fx-font-size: 18px; -fx-text-fill: #00788C;");

        TextField champDepart = new TextField();
        champDepart.setPromptText("Station de départ");
        TextField champArrivee = new TextField();
        champArrivee.setPromptText("Station d'arrivée");
        TextField champHeure = new TextField();
        champHeure.setPromptText("Heure (ex: 16h00)");

        Button boutonValider = new Button("Valider le trajet");
        styliserBouton(boutonValider);
        boutonValider.setOnAction(e -> {
            System.out.println("Trajet simulé de " + champDepart.getText() + " à " + champArrivee.getText() + " à " + champHeure.getText());
        });

        pageTrajet = new VBox(10, titre, champDepart, champArrivee, champHeure, boutonValider);
        pageTrajet.setAlignment(Pos.CENTER);
    }

    private void creerPagePlans() {
        Image image = new Image("file:src/main/resources/plan.png");
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(850);

        ScrollPane scrollPane = new ScrollPane(imageView);
        scrollPane.setFitToWidth(true);

        pagePlans = new VBox(scrollPane);
        pagePlans.setAlignment(Pos.CENTER);
        pagePlans.setPadding(new Insets(10));
    }

    private void creerPageHoraires() {
        Label titre = new Label("🕒 Horaires");
        titre.setStyle("-fx-font-size: 18px; -fx-text-fill: #00788C;");

        TextField station = new TextField();
        station.setPromptText("Entrez la station");

        TextArea resultats = new TextArea();
        resultats.setEditable(false);
        resultats.setPrefHeight(200);

        Button rechercher = new Button("Rechercher horaires");
        styliserBouton(rechercher);
        rechercher.setOnAction(e -> {
            String s = station.getText();
            resultats.setText("Horaires disponibles pour " + s + ":\n - 08:00\n - 09:30\n - 11:00\n - 14:30\n - 16:00");
        });

        pageHoraires = new VBox(10, titre, station, rechercher, resultats);
        pageHoraires.setAlignment(Pos.CENTER);
        pageHoraires.setPadding(new Insets(10));
    }

    private void creerPageCarte() {
        WebView webView = new WebView();
        webView.getEngine().load("https://www.openstreetmap.org");

        pageCarte = new BorderPane(webView);
    }

    private void afficherPageAdresse() {
        Label titre = new Label("Entrer l'adresse de " + typeAdresse);
        TextField champAdresse = new TextField();
        champAdresse.setPromptText("Ex: 10 rue de Paris");

        Button boutonValider = new Button("Valider");
        styliserBouton(boutonValider);
        boutonValider.setOnAction(e -> {
            if (typeAdresse.equals("Maison")) {
                adresseMaison = champAdresse.getText();
            } else {
                adresseTravail = champAdresse.getText();
            }
            sauvegarderAdresses();
            root.setCenter(pageAccueil);
        });

        VBox content = new VBox(10, titre, champAdresse, boutonValider);
        content.setAlignment(Pos.CENTER);
        pageAdresse = content;
        root.setCenter(pageAdresse);
        animerTransition(pageAdresse);
    }

    private HBox creerMenuBas() {
        Button accueil = new Button("Accueil");
        accueil.setGraphic(new ImageView(new Image("file:src/main/resources/icons/home.png", 16, 16, true, true)));
        accueil.setOnAction(e -> {
            root.setCenter(pageAccueil);
            animerTransition(pageAccueil);
        });

        Button trajet = new Button("Trajet");
        trajet.setOnAction(e -> {
            root.setCenter(pageTrajet);
            animerTransition(pageTrajet);
        });

        Button plans = new Button("Plans");
        plans.setGraphic(new ImageView(new Image("file:src/main/resources/icons/map.png", 16, 16, true, true)));
        plans.setOnAction(e -> {
            root.setCenter(pagePlans);
            animerTransition(pagePlans);
        });

        Button horaires = new Button("Horaires");
        horaires.setGraphic(new ImageView(new Image("file:src/main/resources/icons/timer.png", 16, 16, true, true)));
        horaires.setOnAction(e -> {
            root.setCenter(pageHoraires);
            animerTransition(pageHoraires);
        });

        Button carte = new Button("Carte");
        carte.setGraphic(new ImageView(new Image("file:src/main/resources/icons/mark-on-map.png", 16, 16, true, true)));
        carte.setOnAction(e -> {
            root.setCenter(pageCarte);
            animerTransition(pageCarte);
        });

        for (Button b : new Button[]{accueil, trajet, plans, horaires, carte}) {
            styliserBouton(b);
        }

        HBox menu = new HBox(10, accueil, trajet, plans, horaires, carte);
        menu.setAlignment(Pos.CENTER);
        menu.setStyle("-fx-padding: 10;");
        return menu;
    }

    private void styliserBouton(Button bouton) {
        bouton.setStyle("-fx-background-color: #00A6B8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
    }

    private void styliserPetitBouton(Button bouton) {
        bouton.setStyle("-fx-background-color: #00A6B8; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 4 8;");
    }

    private void animerTransition(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(500), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void chargerAdresses() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FICHIER_ADRESSES))) {
            adresseMaison = reader.readLine();
            adresseTravail = reader.readLine();
        } catch (IOException e) {
            adresseMaison = "";
            adresseTravail = "";
        }
    }

    private void sauvegarderAdresses() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FICHIER_ADRESSES))) {
            writer.write(adresseMaison + "\n");
            writer.write(adresseTravail);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'enregistrement des adresses.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
