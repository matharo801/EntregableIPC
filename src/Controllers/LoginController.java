/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import model.User;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private Label lblMensaje;
    @FXML
    private PasswordField txtContraseña;
    @FXML
    private Button iniciarSesion;
    @FXML
    private Button btnIrMenuPrincipal;
    @FXML
    private Button btnRegistrarse;
 

    @FXML
    public void iniciarSesion(ActionEvent event) {
        String user = txtUsuario.getText().trim();
        String pass = txtContraseña.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMensaje.setText("Completa todos los campos.");
            return;
        }

        if (usuarioValido(user) && contraseñaValido(pass)) {
            lblMensaje.setText("Acceso concedido.");
        } else {
            lblMensaje.setText("Usuario o contraseña incorrectos.");
        }
        
        UtilidadesEscena.cambiarEscena("/CartaNautica/MainView.fxml", event, "Aplicacion general");
    }

    private boolean usuarioValido(String user) {
        // Simulación de validación con BD
        return User.checkNickName(user);
    }
   
    private boolean contraseñaValido(String password){
        return User.checkPassword(password);
    }
    
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
    public void irAregistrarse(ActionEvent event) {
        cambiarEscena( event, "/view/Registrarse.fxml");
    }
    

    @FXML
    private void irAlMenuPrincipal(ActionEvent event) 
    {
        cambiarEscena( event, "/view/Principal.fxml");
    }
    
        
}
