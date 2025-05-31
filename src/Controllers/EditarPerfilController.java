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
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EditarPerfilController implements Initializable {

    @FXML
    private Label usernameLabel;
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
    private String currentNickName; // Este debería establecerse tras login
    private File selectedAvatarFile;
    @FXML
    private Button btnIrMenuPrincipal;

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

    private void loadUserProfile() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "SELECT * FROM user WHERE nickName = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentNickName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                usernameLabel.setText(rs.getString("nickName"));
                passwordField.setText(rs.getString("password"));
                emailField.setText(rs.getString("email"));
                birthDatePicker.setValue(LocalDate.parse(rs.getString("birthDate")));

                InputStream avatarStream = rs.getBinaryStream("avatar");
                if (avatarStream != null) {
                    avatarImageView.setImage(new Image(avatarStream));
                }
            }
        } catch (Exception e) {
            statusLabel.setText("Error al cargar el perfil.");
        }
    }

    private void handleChangeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Avatar");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        selectedAvatarFile = fileChooser.showOpenDialog(null);
        if (selectedAvatarFile != null) {
            avatarImageView.setImage(new Image(selectedAvatarFile.toURI().toString()));
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void handleUpdateProfile() {
        String password = passwordField.getText();
        String email = emailField.getText();
        LocalDate birthDate = birthDatePicker.getValue();

        if (!isValidPassword(password)) {
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

            if (selectedAvatarFile != null) {
                stmt.setBinaryStream(4, new FileInputStream(selectedAvatarFile));
            } else {
                stmt.setNull(4, Types.BLOB);
            }

            stmt.setString(5, currentNickName);
            stmt.executeUpdate();

            statusLabel.setText("Perfil actualizado correctamente.");
        } catch (Exception e) {
            statusLabel.setText("Error al actualizar perfil.");
        }
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.length() <= 20 &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*[a-z].*") &&
               password.matches(".*\\d.*") &&
               password.matches(".*[!@#$%&*()\\-+=].*");
    }
    
    @FXML 
    private void cancelarEditarPerfil(ActionEvent event){
         Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
         stage.close();
    }
    
    @FXML
    private void irAlMenuPrincipal(ActionEvent event) 
    {
        cambiarEscena(event, "/view/Principal.fxml");
    }
}

