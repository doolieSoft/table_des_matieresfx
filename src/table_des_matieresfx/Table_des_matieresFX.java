/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package table_des_matieresfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Stefano Crapanzano
 */
public class Table_des_matieresFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXML_Main.fxml"));

        Scene scene = new Scene(root);
        setUserAgentStylesheet(STYLESHEET_CASPIAN);
        
        stage.setTitle("Table des matieres FX");
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
