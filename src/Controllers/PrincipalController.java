/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PrincipalController implements Initializable {


    @FXML
    private Button btnCerrarSesion;
    @FXML
    private Button btnIniciarSesion;
    @FXML
    private Button btnRealizarProblema;
    @FXML
    private Button btnModificarPerfil;
    @FXML
    private Button btnMostrarResultados;

    private void cambiarEscena(ActionEvent event, String rutaFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void iniciarSesion(ActionEvent event) {
        cambiarEscena(event, "/view/Login.fxml");
    }

    @FXML
    private void realizarProblema(ActionEvent event) {
        cambiarEscena(event, "/view/Problema.fxml");
    }
    
    @FXML
    private void mostrarResultados(ActionEvent event) {
        cambiarEscena(event, "/view/resultados.fxml");
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cerrar sesión");
        alert.setHeaderText(null);
        alert.setContentText("Gracias por usar nuestra aplicación.\n¡Hasta pronto!");
        alert.showAndWait();
        Platform.exit();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicialización si se requiere
    }

    @FXML
    private void modPerfil(ActionEvent event) {
        cambiarEscena(event, "/view/EditarPerfil.fxml");
    }
}