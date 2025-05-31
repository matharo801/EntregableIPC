/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
/*
 * MainViewController – Zoom estable + Regla completa
 *  - Ctrl + rueda: zoom bajo el cursor (pivot correcto)
 *  - Slider: zoom centrado; etiqueta sincronizada
 *  - Regla: toggle que muestra distancia enlazada a lastDistanceNmProperty()
 */
package CartaNautica;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;

public class MainViewController implements Initializable {

    /* ──────────── FXML ──────────── */
    @FXML private TabPane tabPane;
    @FXML private Tab problemaTab;
    @FXML private Tab cartaTab;

    /* Herramientas */
    @FXML private Button puntoButton, lineaButton, textoButton, limpiarButton,
                          CircButton, coordBttn, lineaAngButton;
    @FXML private ToggleButton transButton, reglaBttn;

    /* Formato */
    @FXML private Slider slider, grosorSlider;
    @FXML private ColorPicker colorPicker;

    /* Otros nodos */
    @FXML private BorderPane root;
    @FXML private Label zoomLbl, distanciaLbl;

    /* ──────────── Estado ──────────── */
    private Carta_nauticaController carta;
    private final ObjectProperty<HerramientaActiva> selectedTool =
            new SimpleObjectProperty<>(HerramientaActiva.NINGUNA);
    private final DoubleProperty strokeWidth = new SimpleDoubleProperty(2);

    /* ─────────────────────────────────────────────── */
    @Override public void initialize(URL url, ResourceBundle rb) {

        carta = loadTab(cartaTab, "CartaNautica.fxml");
        loadTab(problemaTab, "Problema.fxml");

        slider.setMin(5);  slider.setMax(30); // 0.5× – 3×

        if (carta != null) {

            /* Distancia NM publicada por la regla */
            distanciaLbl.textProperty().bind(
                    Bindings.format("Distancia: %.2f NM", carta.lastDistanceNmProperty()));

            /* Tool ↔ carta */
            Bindings.bindBidirectional(selectedTool, carta.currentToolProperty());

            /* Grosor de trazo */
            grosorSlider.setMin(1); grosorSlider.setMax(6); grosorSlider.setValue(2);
            grosorSlider.setMajorTickUnit(1); grosorSlider.setSnapToTicks(true);
            Bindings.bindBidirectional(strokeWidth, grosorSlider.valueProperty());
            carta.strokeWidthProperty().bind(strokeWidth);

            /* ─── Slider → Zoom centrado ─── */
            slider.valueProperty().addListener((o, ov, nv) -> applyCenteredZoom(nv.doubleValue()*0.10));

            /* ─── Ctrl + rueda bajo cursor ─── */
            carta.scrollPane.addEventFilter(ScrollEvent.SCROLL, ev -> {
                if (!ev.isControlDown()) return;
                double factor = ev.getDeltaY() > 0 ? 1.1 : 0.9;
                double newS   = carta.scaleTransform.getX() * factor;
                Point2D pivot = carta.anchorMapa.sceneToLocal(ev.getScreenX(), ev.getScreenY());
                carta.scaleTransform.setPivotX(pivot.getX());
                carta.scaleTransform.setPivotY(pivot.getY());
                carta.scaleTransform.setX(newS);
                carta.scaleTransform.setY(newS);
                slider.setValue(newS * 10);
                zoomLbl.setText(String.format("Zoom %.0f %%", newS * 100));
                ev.consume();
            });

            /* Color global */
            carta.kolor.bind(colorPicker.valueProperty());

            /* Transportador toggle */
            transButton.selectedProperty().addListener((o, ov, nv) -> {
                carta.setTransportadorVisible(nv);
                if (nv) { selectedTool.set(HerramientaActiva.TRANSPORTADOR); reglaBttn.setSelected(false); }
                else if (selectedTool.get() == HerramientaActiva.TRANSPORTADOR)
                    selectedTool.set(HerramientaActiva.NINGUNA);
            });

            /* Regla toggle */
            reglaBttn.selectedProperty().addListener((o, ov, nv) -> {
                carta.setReglaVisible(nv);
                if (nv) { selectedTool.set(HerramientaActiva.REGLA_MEDIR); transButton.setSelected(false); }
                else if (selectedTool.get() == HerramientaActiva.REGLA_MEDIR)
                    selectedTool.set(HerramientaActiva.NINGUNA);
            });

            lineaAngButton.disableProperty().bind(transButton.selectedProperty().not());
        }
    }

    /* Aplica zoom manteniendo el centro del viewport */
    private void applyCenteredZoom(double scale){
        Bounds vp = carta.scrollPane.getViewportBounds();
        Point2D sceneCenter = new Point2D(vp.getWidth()/2 + carta.scrollPane.getLayoutX(),
                                          vp.getHeight()/2 + carta.scrollPane.getLayoutY());
        Point2D localCenter = carta.anchorMapa.sceneToLocal(sceneCenter);
        carta.scaleTransform.setPivotX(localCenter.getX());
        carta.scaleTransform.setPivotY(localCenter.getY());
        carta.scaleTransform.setX(scale);
        carta.scaleTransform.setY(scale);
        zoomLbl.setText(String.format("Zoom %.0f %%", scale*100));
    }

    /* ───────── Handlers básicos ───────── */
    @FXML private void handlePunto(ActionEvent e){ selectedTool.set(HerramientaActiva.PUNTO);}   
    @FXML private void handleLinea(ActionEvent e){ selectedTool.set(HerramientaActiva.LINEA);}   
    @FXML private void handleCircunferencia(ActionEvent e){ selectedTool.set(HerramientaActiva.CIRCUNFERENCIA);}  
    @FXML private void handleTexto(ActionEvent e){ selectedTool.set(HerramientaActiva.TEXTO);}   
    @FXML private void handleDistancia(ActionEvent e){ selectedTool.set(HerramientaActiva.DISTANCIA);}   
    @FXML private void handleCoordenadas(ActionEvent e){ selectedTool.set(HerramientaActiva.COORDENADAS);}   
    @FXML private void handleLineaAngulo(ActionEvent e){ selectedTool.set(HerramientaActiva.LINEA_ANGULO);}   
    @FXML private void handleLimpiar(ActionEvent e){ carta.limpiar(); selectedTool.set(HerramientaActiva.NINGUNA);}   

    /* Utilidad de carga */
    private <T> T loadTab(Tab tab,String fxml){
        try{
            FXMLLoader l=new FXMLLoader(getClass().getResource(fxml));
            Parent root=l.load();
            tab.setContent(root);
            return l.getController();
        }catch(IOException ex){ex.printStackTrace(); return null;}
    }

    /* Exposición opcional */
    public ObjectProperty<HerramientaActiva> selectedToolProperty(){ return selectedTool; }
}