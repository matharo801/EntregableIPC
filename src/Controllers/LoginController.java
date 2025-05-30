/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblMensaje;

    @FXML
    public void iniciarSesion() {
        String user = txtUsuario.getText().trim();
        String pass = txtContrasena.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMensaje.setText("Completa todos los campos.");
            return;
        }

        if (usuarioValido(user, pass)) {
            lblMensaje.setText("Acceso concedido.");
        } else {
            lblMensaje.setText("Usuario o contraseña incorrectos.");
        }
    }

    private boolean usuarioValido(String user, String pass) {
        // Simulación de validación con BD
        return user.equals("jpgarcia") && pass.equals("passPER21!");
    }
    
    public void registrarse() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Registrarse.fxml"));
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

