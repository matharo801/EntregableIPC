/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EditarPerfilController implements Initializable {
    
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

    private final String DB_URL = "jdbc:sqlite:data.db";
    private String usernameActual; // Este debería establecerse tras login
    private File ImagenSeleccionada;
    @FXML
    private TextField usernameField;
    @FXML
    private Button insertarImagen;
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnMenuPrincipal;
    

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //loadUserProfile();
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

    public void cargarDatosUsuario() {

    try (Connection conn = DriverManager.getConnection(DB_URL)) {
        String sql = "SELECT username, password, email, birthdate, avatar FROM user WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usernameActual);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                usernameField.setText(rs.getString("username"));
                passwordField.setText(rs.getString("password"));
                emailField.setText(rs.getString("email"));

                String birthdate = rs.getString("birthdate");
                if (birthdate != null) {
                    birthDatePicker.setValue(LocalDate.parse(birthdate));
                }

                String avatarPath = rs.getString("avatar");
                if (avatarPath != null && !avatarPath.isEmpty()) {
                    avatarImageView.setImage(new Image(avatarPath));
                } else {
                    statusLabel.setText("No se encontró avatar.");
                }
            } else {
                statusLabel.setText("Usuario no encontrado.");
            }
        }
    } catch (SQLException e) {
        statusLabel.setText("Error al cargar los datos.");
        e.printStackTrace();
    }
}


    @SuppressWarnings("CallToPrintStackTrace")
    private void actualizar() {
        String password = passwordField.getText();
        String email = emailField.getText();
        LocalDate birthDate = birthDatePicker.getValue();

        if (!esPasswordValida(password)) {
            statusLabel.setText("Contraseña inválida.");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            statusLabel.setText("Email inválido.");
            return;
        }

        if (birthDate == null || birthDate.isAfter(LocalDate.now().minusYears(16))) {
            statusLabel.setText("Debes tener al menos 16 años.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "UPDATE user SET password = ?, email = ?, birthDate = ?, avatar = ? WHERE nickName = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, password);
            stmt.setString(2, email);
            stmt.setString(3, birthDate.toString());

            if (ImagenSeleccionada != null) {
                stmt.setBinaryStream(4, new FileInputStream(ImagenSeleccionada));
            } else {
                stmt.setNull(4, Types.BLOB);
            }

            stmt.setString(5, usernameActual);
            stmt.executeUpdate();

            statusLabel.setText("Perfil actualizado correctamente.");
        } catch (Exception e) {
            statusLabel.setText("Error al actualizar perfil.");
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

    
    @FXML
    private void insertarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen de avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        ImagenSeleccionada = fileChooser.showOpenDialog(new Stage());
        if (ImagenSeleccionada != null) {
            avatarImageView.setImage(new Image(ImagenSeleccionada.toURI().toString()));
        }
    }

    @FXML
    private void registrarUsuario(ActionEvent event) {
        
    }

    @FXML
    private void irAMenuPrincipal(ActionEvent event) {
        cambiarEscena(event, "/view/Principal.fxml");
    }
}

