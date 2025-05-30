/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.sql.*;

/**
 * FXML Controller class
 *
 * @author The_T
 */
public class RegistrarseController implements Initializable {

    private TextField usernameField;
    private PasswordField passwordField;
    private TextField emailField;
    private DatePicker birthDatePicker;
    private ImageView avatarImageView;
    private Label statusLabel;
    
    private final String defaultAvatarPath = "/Libraries/IPC2025/avatars/default.png";
    
    private final String DB_URL = "jdbc:sqlite:/mnt/data/data.db";
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        avatarImageView.setImage(new Image(defaultAvatarPath));
    }    
    
    private void insertarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen de avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            avatarImageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }
    
    private boolean validarUsername(String username) {
        return username.matches("[a-zA-Z0-9_-]{6,15}");
    }

    private boolean validarPassword(String password) {
        return password.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%&*()\\-+=]).{8,20}");
    }

    private boolean validarEmail(String email) {
        return Pattern.matches("^\\S+@\\S+\\.\\S+$", email);
    }

    private boolean esMayorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) return false;
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() >= 16;
    }
    
    public void Registrar() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        LocalDate birthDate = birthDatePicker.getValue();
        String avatar = avatarImageView.getImage().getUrl();

        if (!validarUsername(username)) {
            statusLabel.setText("Nombre de usuario no válido o ya existe.");
            return;
        }

        if (!validarPassword(password)) {
            statusLabel.setText("Contraseña no válida.");
            return;
        }

        if (!validarEmail(email)) {
            statusLabel.setText("Correo electrónico no válido.");
            return;
        }

        if (!esMayorDeEdad(birthDate)) {
            statusLabel.setText("Debes tener al menos 16 años.");
            return;
        }

         try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Check if user exists
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM user WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                statusLabel.setText("El nombre de usuario ya está en uso.");
            }

            // Insert user
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO user (username, email, password, birthdate, avatar) VALUES (?, ?, ?, ?, ?)"
            );
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, password); // En producción: cifrarla
            insertStmt.setString(4, birthDate.toString());
            insertStmt.setString(5, avatar);
            insertStmt.executeUpdate();

            statusLabel.setText("¡Registro exitoso!");
        } catch (SQLException e) {
            statusLabel.setText("Error en la base de datos.");
            e.printStackTrace();
            }
        }
      
}

