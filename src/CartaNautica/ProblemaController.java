/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package CartaNautica;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

/**
 * Controlador de la pestaña «Problema».
 * Muestra un enunciado con tres opciones y marca correctas/incorrectas.
 */
public class ProblemaController implements Initializable {

    /*───────────── FXML ─────────────*/
    @FXML private Label enunciadoLbl;
    @FXML private RadioButton op1, op2, op3;
    @FXML private ToggleGroup tg;

    /*───────────── modelo sencillo ─────────────*/
    private List<Problem> banco;
    private Problem actual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Banco mínimo de problemas embebido
        banco = List.of(
            new Problem(
                "¿Cuántos metros hay en una milla náutica?",
                List.of("1 000 m", "1 852 m", "1 609 m"), 1),
            new Problem(
                "Centroide de un triángulo equilátero:",
                List.of("A ½ de la altura",
                        "A ⅓ de la altura",
                        "A ⅔ de la altura"), 1),
            new Problem(
                "Producto escalar de dos vectores perpendiculares es:",
                List.of("0", "1", "El módulo del mayor"), 0)
        );

        mostrarSiguiente();

        // Listener para marcar la opción elegida
        tg.selectedToggleProperty().addListener((o, ov, nv) -> {
            if (nv == null) return;

            int idx = List.of(op1, op2, op3).indexOf(nv);
            boolean acierto = idx == actual.correctIndex();
            ((RadioButton) nv).setStyle(acierto ?
                    "-fx-text-fill: green;" :
                    "-fx-text-fill: red;");

            // Espera 1,5 s y pasa al siguiente
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ex) {}
                Platform.runLater(this::mostrarSiguiente);
            }).start();
        });
    }

    /** Muestra un problema aleatorio en la interfaz */
    private void mostrarSiguiente() {
        actual = banco.get((int) (Math.random() * banco.size()));

        enunciadoLbl.setText(actual.text());
        op1.setText(actual.options().get(0)); op1.setSelected(false); op1.setStyle("");
        op2.setText(actual.options().get(1)); op2.setSelected(false); op2.setStyle("");
        op3.setText(actual.options().get(2)); op3.setSelected(false); op3.setStyle("");
    }

    /*───────────── POJO mínimo ─────────────*/
    private static record Problem(String text, List<String> options, int correctIndex) {}
}