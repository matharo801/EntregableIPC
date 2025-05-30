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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PrincipalController implements Initializable {

    @FXML
    private Button btnIniciarSesion;

    @FXML
    private Button btnRealizarProblema;

    @FXML
    private Button btnModificarPerfil;

    @FXML
    private Button btnMostrarResultados;

    @FXML
    private Button btnCerrarSesion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Aquí puedes hacer lógica de inicio si es necesario
    }

    /**
     * Cambia la escena actual a un archivo FXML específico dentro de /view/
     */
    private void cambiarEscena(String fxmlNombre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlNombre));
            Parent root = loader.load();
            Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Academia Náutica - " + fxmlNombre.replace(".fxml", ""));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void iniciarSesion(ActionEvent event) {
        cambiarEscena("Login.fxml");
    }

    @FXML
    private void realizarProblema(ActionEvent event) {
        cambiarEscena("Problema.fxml");
    }

    @FXML
    private void modificarPerfil(ActionEvent event) {
        cambiarEscena("EditarPerfil.fxml");
    }

    @FXML
    private void mostrarResultados(ActionEvent event) {
        cambiarEscena("Resultados.fxml");
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        cambiarEscena("Login.fxml");
    }
}
