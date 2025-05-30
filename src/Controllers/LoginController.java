/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import Database.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblMensaje;

    @FXML
    private Button iniciarSesion;

    @FXML
    private Button registrarse;

    @FXML
    public void iniciarSesion() {
        String user = txtUsuario.getText().trim();
        String pass = txtContrasena.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            mostrarMensaje("Completa todos los campos.");
            return;
        }

        if (usuarioValido(user, pass)) {
            mostrarMensaje("Acceso concedido.");
            cargarVentanaMain();  // Ir a Main.fxml
        } else {
            mostrarMensaje("Usuario o contraseña incorrectos.");
        }
    }

    @FXML
    public void registrarse() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Registrarse.fxml"));
            Stage stage = (Stage) registrarse.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Usuario");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarMensaje("Error al cargar la ventana de registro.");
        }
    }

   
    private boolean usuarioValido(String user, String pass) {
        String query = "SELECT * FROM usuarios WHERE nombre_usuario = ? AND contrasena = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user);
            stmt.setString(2, pass);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true si hay coincidencia

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error de conexión", "No se pudo acceder a la base de datos.");
            return false;
        }
    }

    /**
     * Carga la pantalla principal de la aplicación (Main.fxml)
     */
    @FXML
    private void cargarVentanaMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Main.fxml"));
            Stage stage = (Stage) iniciarSesion.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Aplicación Principal");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarMensaje("No se pudo cargar la aplicación general.");
        }
    }

    private void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
    }

    private void showAlert(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
