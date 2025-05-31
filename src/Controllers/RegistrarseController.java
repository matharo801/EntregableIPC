/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RegistrarseController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private ImageView avatarImageView;
    @FXML
    private Label statusLabel;
    @FXML
    private Button insertarImagen;
    @FXML
    private Button btnRegistrarse;
    @FXML
    private Button btnMenuPrincipal;

    private final String defaultAvatarPath = "/Libraries/IPC2025/avatars/default.png";
    private File imagenSeleccionada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        URL imageUrl = getClass().getResource(defaultAvatarPath);
        if (imageUrl != null) {
            avatarImageView.setImage(new Image(imageUrl.toExternalForm()));
        } else {
            System.err.println("No se encontró la imagen por defecto: " + defaultAvatarPath);
        }
    }

    @FXML
    public void insertarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen de avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            imagenSeleccionada = selectedFile;
            avatarImageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    public void registrarUsuario(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        LocalDate birthDate = birthDatePicker.getValue();
        String avatar = (imagenSeleccionada != null) ? imagenSeleccionada.toURI().toString() : null;

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

        String DB_URL = "jdbc:sqlite:/mnt/data/data.db";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String checkSql = "SELECT COUNT(*) FROM user WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        statusLabel.setText("El nombre de usuario ya está en uso.");
                        return;
                    }
                }
            }

            String insertSql = "INSERT INTO user (username, email, password, birthdate, avatar) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, email);
                insertStmt.setString(3, password); // Deberías hashear esto
                insertStmt.setString(4, birthDate.toString());

                if (avatar != null) {
                    insertStmt.setString(5, avatar);
                } else {
                    insertStmt.setNull(5, Types.VARCHAR);
                }

                insertStmt.executeUpdate();
                statusLabel.setText("¡Registro exitoso!");
            }

        } catch (SQLException e) {
            statusLabel.setText("Error en la base de datos.");
            e.printStackTrace();
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
        return fechaNacimiento != null && Period.between(fechaNacimiento, LocalDate.now()).getYears() >= 16;
    }

    @FXML
    private void irAMenuPrincipal(ActionEvent event) {
        UtilidadesEscena.cambiarEscena("/view/Principal.fxml", event, "Menu Principal");
    }

    @FXML
    private void validarUsername(ActionEvent event) {
        String username = usernameField.getText().trim();
    if (!esUsernameValido(username)) {
        statusLabel.setText("Nombre de usuario inválido. Debe tener entre 6 y 15 caracteres alfanuméricos.");
    } else {
        statusLabel.setText("");
    }
    }

    @FXML
    private void validarPassword(ActionEvent event) {
        String password = passwordField.getText().trim();
    if (!esPasswordValida(password)) {
        statusLabel.setText("Contraseña inválida. Debe tener mayúsculas, minúsculas, número y símbolo.");
    } else {
        statusLabel.setText("");
    }
    }

    @FXML
    private void validarEmail(ActionEvent event) {
        String email = emailField.getText().trim();
    if (!esEmailValido(email)) {
        statusLabel.setText("Correo electrónico inválido.");
    } else {
        statusLabel.setText("");
    }
    }

    @FXML
    private void esMayorDeEdad(ActionEvent event) {
        LocalDate birthDate = birthDatePicker.getValue();
    if (!esMayorDeEdadFunc(birthDate)) {
        statusLabel.setText("Debes tener al menos 16 años.");
    } else {
        statusLabel.setText("");
    }
    }
}

