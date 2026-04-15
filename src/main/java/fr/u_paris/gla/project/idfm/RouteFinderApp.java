package fr.u_paris.gla.project.idfm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class RouteFinderApp extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("RouteFinderView.fxml"));
        Scene scene = new Scene(root);
        
        // Titre de l'application
        stage.setTitle("IDFM Navigator - Université Paris Cité");
        
        // Icône de l'application
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/fr/u_paris/gla/project/uparis_logo_rvb.png")));
        
        // Taille minimale
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
