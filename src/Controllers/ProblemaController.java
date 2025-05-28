/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;

public class ProblemaController implements Initializable {

    @FXML
    private Label lblEnunciado;

    @FXML
    private VBox vboxOpciones;

    @FXML
    private Button btnVerificar;

    @FXML
    private Label lblResultado;

    private ToggleGroup opcionesGroup = new ToggleGroup();

    // Simulación de datos (normalmente vendrían de una base de datos)
    private static class Problema {
        String enunciado;
        String respuestaCorrecta;
        List<String> opciones;

        Problema(String enunciado, String correcta, List<String> opciones) {
            this.enunciado = enunciado;
            this.respuestaCorrecta = correcta;
            this.opciones = opciones;
        }
    }

    private Problema problemaActual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarProblemaAleatorio();
    }

    private void cargarProblemaAleatorio() {
        // Datos de ejemplo
        List<String> opciones = Arrays.asList("10°", "20°", "30°", "40°");
        problemaActual = new Problema(
            "¿Cuál es la corrección total si la declinación es 10°E y el desvío es 20°W?",
            "10°",
            opciones
        );

        lblEnunciado.setText(problemaActual.enunciado);
        mostrarOpciones(problemaActual.opciones);
        lblResultado.setText(""); // Limpiar resultado
    }

    private void mostrarOpciones(List<String> opciones) {
        vboxOpciones.getChildren().clear();
        opcionesGroup.getToggles().clear();

        List<String> opcionesMezcladas = new ArrayList<>(opciones);
        Collections.shuffle(opcionesMezcladas);

        for (String texto : opcionesMezcladas) {
            RadioButton rb = new RadioButton(texto);
            rb.setToggleGroup(opcionesGroup);
            vboxOpciones.getChildren().add(rb);
        }
    }

    @FXML
    private void verificarRespuesta() {
        RadioButton seleccionada = (RadioButton) opcionesGroup.getSelectedToggle();

        if (seleccionada == null) {
            lblResultado.setText("Seleccione una respuesta.");
            return;
        }

        String respuesta = seleccionada.getText();
        if (respuesta.equals(problemaActual.respuestaCorrecta)) {
            lblResultado.setText("¡Correcto!");
        } else {
            lblResultado.setText("Incorrecto. La respuesta correcta es: " + problemaActual.respuestaCorrecta);
        }
    }
}

