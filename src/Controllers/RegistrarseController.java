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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

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
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        avatarImageView.setImage(new Image(defaultAvatarPath));
    }    
    
    @FXML
    public void insertarImagen() {
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
    
    @FXML
    public void Registrar() {
    String username = usernameField.getText().trim();
    String password = passwordField.getText().trim();
    String email = emailField.getText().trim();
    LocalDate birthDate = birthDatePicker.getValue();
    String avatar = null;
    
    // Safely get avatar URL if image is set
    if (avatarImageView.getImage() != null) {
        avatar = avatarImageView.getImage().getUrl();
    }

    // Basic null/empty checks
    if (username.isEmpty() || password.isEmpty() || email.isEmpty() || birthDate == null) {
        statusLabel.setText("Por favor, rellena todos los campos.");
        return;
    }

    if (!esUsernameValido(username)) {
        statusLabel.setText("Nombre de usuario no válido o ya existe.");
        return;
    }

    if (!esPasswordValida(password)) {
        statusLabel.setText("Contraseña no válida.");
        return;
    }

    if (!esEmailValido(email)) {
        statusLabel.setText("Correo electrónico no válido.");
        return;
    }

    if (!esMayorDeEdadFunc(birthDate)) {
        statusLabel.setText("Debes tener al menos 16 años.");
        return;
    }

    // Connect to DB and register user
    String DB_USER = "your_db_username";  // Put your DB username here
    String DB_PASS = "your_db_password";  // Put your DB password here

    String DB_URL = "jdbc:sqlite:/mnt/data/data.db"; // Your DB URL

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
        
        // Check if username already exists
        String checkSql = "SELECT COUNT(*) FROM user WHERE username = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    statusLabel.setText("El nombre de usuario ya está en uso.");
                    return;  // Important: stop if username exists!
                }
            }
        }

        // Insert new user
        String insertSql = "INSERT INTO user (username, email, password, birthdate, avatar) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);

            // TODO: Hash the password before saving!
            insertStmt.setString(3, password);

            insertStmt.setString(4, birthDate.toString());

            if (avatar != null) {
                insertStmt.setString(5, avatar);
            } else {
                insertStmt.setNull(5, java.sql.Types.VARCHAR);
            }

            insertStmt.executeUpdate();
        }

        statusLabel.setText("¡Registro exitoso!");
    } catch (SQLException e) {
        statusLabel.setText("Error en la base de datos.");
        e.printStackTrace();
    }
}

    @FXML
private void validarUsername(ActionEvent event) {
    String username = usernameField.getText();
    if (!esUsernameValido(username)) {
        System.out.println("Username inválido.");
    } else {
        System.out.println("Username válido.");
    }
}

@FXML
private void validarPassword(ActionEvent event) {
    String password = passwordField.getText();
    if (!esPasswordValida(password)) {
        System.out.println("Password inválida.");
    } else {
        System.out.println("Password válida.");
    }
}

@FXML
private void validarEmail(ActionEvent event) {
    String email = emailField.getText();
    if (!esEmailValido(email)) {
        System.out.println("Email inválido.");
    } else {
        System.out.println("Email válido.");
    }
}

@FXML
private void esMayorDeEdad(ActionEvent event) {
    LocalDate fechaNacimiento = birthDatePicker.getValue();
    if (!esMayorDeEdadFunc(fechaNacimiento)) {
        System.out.println("Debe tener al menos 16 años.");
    } else {
        System.out.println("Edad válida.");
    }
}

private boolean esUsernameValido(String username) {
    return username.matches("[a-zA-Z0-9_-]{6,15}");
}

private boolean esPasswordValida(String password) {
    return password.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%&*()\\-+=]).{8,20}");
}

private boolean esEmailValido(String email) {
    return Pattern.matches("^\\S+@\\S+\\.\\S+$", email);
}

private boolean esMayorDeEdadFunc(LocalDate fechaNacimiento) {
    if (fechaNacimiento == null) return false;
    return Period.between(fechaNacimiento, LocalDate.now()).getYears() >= 16;
}

    
}
