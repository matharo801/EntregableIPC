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

    private void cambiarEscena(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Principal.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void iniciarSesion(ActionEvent event) {
        try {
            cambiarEscena("/view/Login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void realizarProblema(ActionEvent event) {
        try {
            cambiarEscena("/view/Problema.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modificarPerfil(ActionEvent event) {
        try {
            cambiarEscena("/view/EditarPerfil.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void mostrarResultados(ActionEvent event) {
        try {
            cambiarEscena("/view/resultados.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        try {
            cambiarEscena("/view/Login.fxml"); // O volver a pantalla inicial
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicialización si se requiere
    }
}

