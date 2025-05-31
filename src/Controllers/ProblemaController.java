/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import model.Answer;
import model.Problem;
import Database.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ProblemaController implements Initializable {

    @FXML private Label lblEnunciado;
    @FXML private RadioButton rb1, rb2, rb3, rb4;

    private ToggleGroup opcionesGroup = new ToggleGroup();
    private List<Problem> problemas;
    private int problemaActual = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        rb1.setToggleGroup(opcionesGroup);
        rb2.setToggleGroup(opcionesGroup);
        rb3.setToggleGroup(opcionesGroup);
        rb4.setToggleGroup(opcionesGroup);

        problemas = cargarProblemas();
        if (!problemas.isEmpty()) {
            mostrarProblema(problemas.get(problemaActual));
        }
    }

    private List<Problem> cargarProblemas() {
        List<Problem> problemas = new ArrayList<>();
        String query = "SELECT * FROM problem";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String text = rs.getString("text");
                Answer a1 = new Answer(rs.getString("answer1"), Boolean.parseBoolean(rs.getString("val1")));
                Answer a2 = new Answer(rs.getString("answer2"), Boolean.parseBoolean(rs.getString("val2")));
                Answer a3 = new Answer(rs.getString("answer3"), Boolean.parseBoolean(rs.getString("val3")));
                Answer a4 = new Answer(rs.getString("answer4"), Boolean.parseBoolean(rs.getString("val4")));

                Problem p = new Problem(text, a1, a2, a3, a4);
                problemas.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return problemas;
    }

    private void mostrarProblema(Problem p) {
        lblEnunciado.setText(p.getText());
        List<Answer> respuestas = p.getAnswers();
        rb1.setText(respuestas.get(0).getText());
        rb2.setText(respuestas.get(1).getText());
        rb3.setText(respuestas.get(2).getText());
        rb4.setText(respuestas.get(3).getText());
    }
}
