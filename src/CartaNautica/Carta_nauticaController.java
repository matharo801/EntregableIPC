/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package CartaNautica;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class Carta_nauticaController implements Initializable {

    /* ─────────── FXML ─────────── */
    @FXML public Group groupCarta;
    @FXML public ImageView imageCarta;
    @FXML public AnchorPane anchorMapa;
    @FXML public ScrollPane scrollPane;

    /* ─────────── API pública ─────────── */
    private final DoubleProperty lastDistanceNm = new SimpleDoubleProperty(0);
    public ReadOnlyDoubleProperty lastDistanceNmProperty() { return lastDistanceNm; }

    public final Scale scaleTransform = new Scale(1, 1, 0, 0);
    public final ObjectProperty<Color> kolor = new SimpleObjectProperty<>(Color.BLACK);
    public final DoubleProperty strokeWidth = new SimpleDoubleProperty(2);
    private final ObjectProperty<HerramientaActiva> currentTool = new SimpleObjectProperty<>(HerramientaActiva.NINGUNA);

    /* ─────────── Carta / proyección ─────────── */
    private static final double LAT_MIN = 35.70, LAT_MAX = 36.20,
                                 LON_MIN = -6.30, LON_MAX = -5.10;
    private double mpxX, mpxY;

    /* ─────────── Transportador / Regla ─────────── */
    private Group transportadorG, reglaG;
    private Tooltip angleTip;

    /* ─────────── auxiliares ─────────── */
    private double lastRadiusPx = -1;
    private Point2D dragAnchor;
    private double initTX, initTY;
    private double prevTX, prevTY;
    private double lineaInicioX = -1, lineaInicioY = -1;
    private double distX = -1, distY = -1;
    private double centroArcoX, centroArcoY;
    private Arc arcoEnCurso;
    private boolean draggingNow = false; // bloquea creación mientras se edita/arrastra

    /* selección + handles */
    private Node selectedNode = null;
    private final List<Node> activeHandles = new ArrayList<>();

    /* ─────────────────────────────────────────── */
    @Override public void initialize(URL url, ResourceBundle rb) {
        Image cartaImg = new Image(getClass().getResourceAsStream("/resources/carta_nautica.jpg"));
        imageCarta.setImage(cartaImg);
        imageCarta.setMouseTransparent(true);
        imageCarta.setFitWidth(2000);
        imageCarta.setFitHeight(1250);
        imageCarta.setPreserveRatio(true);

        double phi = (LAT_MAX + LAT_MIN) / 2.0;
        mpxY = (LAT_MAX - LAT_MIN) / cartaImg.getHeight() * 60.0;
        mpxX = (LON_MAX - LON_MIN) / cartaImg.getWidth() * 60.0 * Math.cos(Math.toRadians(phi));

        /* ░ Transportador ░ */
        ImageView transIV = new ImageView(new Image(getClass().getResource("/resources/transportador.png").toExternalForm()));
        transIV.setOpacity(0.6);
        transIV.setPickOnBounds(true);
        Circle handle = new Circle(6, Color.web("#0066ff99"));
        handle.setStroke(Color.DODGERBLUE);
        handle.setStrokeWidth(1);
        transportadorG = new Group(transIV, handle);
        transportadorG.setVisible(false);
        groupCarta.getChildren().add(transportadorG);
        makeDraggable(transportadorG);

        angleTip = new Tooltip();
        Tooltip.install(transportadorG, angleTip);
        handle.setOnMousePressed(ev -> draggingNow = true);
        handle.setOnMouseDragged(ev -> {
            Point2D m = groupCarta.sceneToLocal(ev.getSceneX(), ev.getSceneY());
            Bounds b = transIV.localToScene(transIV.getBoundsInLocal());
            Point2D c = groupCarta.sceneToLocal(b.getMinX() + b.getWidth() / 2, b.getMinY() + b.getHeight() / 2);
            double ang = Math.toDegrees(Math.atan2(m.getY() - c.getY(), m.getX() - c.getX()));
            transportadorG.setRotate(ang);
            angleTip.setText(String.format("%.1f°", ang));
            angleTip.show(transportadorG, ev.getScreenX() + 10, ev.getScreenY() + 10);
            ev.consume();
        });
        handle.setOnMouseReleased(ev -> { angleTip.hide(); draggingNow = false; ev.consume(); });

        /* ░ Regla ░ */
        ImageView reglaIV = new ImageView(new Image(getClass().getResource("/resources/regla.png").toExternalForm()));
        reglaIV.setOpacity(0.9);
        reglaG = new Group(new Rectangle(reglaIV.getImage().getWidth(), reglaIV.getImage().getHeight(), Color.TRANSPARENT), reglaIV);
        reglaG.setVisible(false);
        groupCarta.getChildren().add(reglaG);
        makeDraggable(reglaG);

        anchorMapa.getTransforms().add(scaleTransform);

        anchorMapa.setOnMouseClicked(e -> handleClick(e.getX(), e.getY()));
        anchorMapa.setOnMousePressed(e -> handlePress(e.getX(), e.getY()));
        anchorMapa.setOnMouseDragged(e -> handleDrag(e.getX(), e.getY()));
        anchorMapa.setOnMouseReleased(e -> handleRelease());

        Platform.runLater(() -> {
            handle.setLayoutX(transIV.getImage().getWidth());
            handle.setLayoutY(0);
            scrollPane.setHvalue(0.5);
            scrollPane.setVvalue(0.5);
        });
    }

    /* ───────────────── Creación de marcas ───────────────── */
    private void handleClick(double x, double y) {
        if (draggingNow) return;
        switch (currentTool.get()) {
            case PUNTO -> {
                Circle p = new Circle(x, y, 4, kolor.get());
                groupCarta.getChildren().add(p);
                addInteractiveBehaviour(p);
            }
            case LINEA -> {
                if (lineaInicioX < 0) { lineaInicioX = x; lineaInicioY = y; }
                else {
                    Line l = new Line(lineaInicioX, lineaInicioY, x, y);
                    styleShape(l);
                    groupCarta.getChildren().add(l);
                    addInteractiveBehaviour(l);
                    lineaInicioX = lineaInicioY = -1;
                }
            }
            case TEXTO -> {
                TextInputDialog d = new TextInputDialog("Texto aquí");
                d.setHeaderText(null);
                d.setTitle("Añadir texto");
                d.showAndWait().ifPresent(txt -> {
                    Text t = new Text(x, y, txt);
                    t.setFill(kolor.get());
                    t.setStyle("-fx-font-size:24px;");
                    groupCarta.getChildren().add(t);
                    addInteractiveBehaviour(t);
                });
            }
            case DISTANCIA -> {
                if (distX < 0) { distX = x; distY = y; }
                else {
                    Line l = new Line(distX, distY, x, y);
                    styleShape(l);
                    double px = Math.hypot(x - distX, y - distY);
                    double nm = px * mpxX;
                    Text tag = new Text((distX + x) / 2, (distY + y) / 2 - 5, String.format("%.2f NM", nm));
                    tag.setFill(kolor.get());
                    /* Crear handlers antes de agrupar */
                    Circle h1 = new Circle(5, Color.ORANGE); h1.setStroke(Color.WHITE); h1.setStrokeWidth(1);
                    Circle h2 = new Circle(5, Color.ORANGE); h2.setStroke(Color.WHITE); h2.setStrokeWidth(1);
                    updateHandlePos(h1, distX, distY); h1.setCursor(Cursor.MOVE);
                    updateHandlePos(h2, x, y);     h2.setCursor(Cursor.MOVE);
                    /* Eventos de drag para handlers (ajustan línea y etiqueta) */
                    h1.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> draggingNow = true);
                    h1.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
                        Point2D p = groupCarta.sceneToLocal(e.getSceneX(), e.getSceneY());
                        l.setStartX(p.getX()); l.setStartY(p.getY());
                        double dx = l.getEndX() - l.getStartX(); double dy = l.getEndY() - l.getStartY();
                        double nm2 = Math.hypot(dx, dy) * mpxX;
                        tag.setText(String.format("%.2f NM", nm2));
                        tag.setX(l.getStartX() + dx / 2);
                        tag.setY(l.getStartY() + dy / 2 - 5);
                        lastDistanceNm.set(nm2);
                        updateHandlePos(h1, p.getX(), p.getY());
                        updateHandlePos(h2, l.getEndX(), l.getEndY());
                        e.consume();
                    });
                    h1.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> { draggingNow = false; e.consume(); });
                    h2.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> draggingNow = true);
                    h2.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
                        Point2D p = groupCarta.sceneToLocal(e.getSceneX(), e.getSceneY());
                        l.setEndX(p.getX()); l.setEndY(p.getY());
                        double dx = l.getEndX() - l.getStartX(); double dy = l.getEndY() - l.getStartY();
                        double nm2 = Math.hypot(dx, dy) * mpxX;
                        tag.setText(String.format("%.2f NM", nm2));
                        tag.setX(l.getStartX() + dx / 2);
                        tag.setY(l.getStartY() + dy / 2 - 5);
                        lastDistanceNm.set(nm2);
                        updateHandlePos(h2, p.getX(), p.getY());
                        updateHandlePos(h1, l.getStartX(), l.getStartY());
                        e.consume();
                    });
                    h2.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> { draggingNow = false; e.consume(); });
                    /* Agrupar todo junto */
                    Group distGroup = new Group(l, tag, h1, h2);
                    groupCarta.getChildren().add(distGroup);
                    addInteractiveBehaviour(distGroup);
                    /* Al arrastrar el Group, handlers se mueven automáticamente */
                    lastRadiusPx = px;
                    lastDistanceNm.set(nm);
                    distX = distY = -1;
                }
            }
            case COORDENADAS -> {
                Line h = new Line(0, y, imageCarta.getFitWidth(), y);
                Line v = new Line(x, 0, x, imageCarta.getFitHeight());
                Stream.of(h, v).forEach(l -> {
                    l.setStroke(kolor.get());
                    l.setStrokeWidth(1);
                    l.getStrokeDashArray().addAll(4.0, 4.0);
                });
                groupCarta.getChildren().addAll(h, v);
                double lat = LAT_MIN + ((imageCarta.getFitHeight() - y) * mpxY) / 60.0;
                double lon = LON_MIN + (x * mpxX) / 60.0;
                Text latT = new Text(8, y, String.format("%.4f°N", lat)); latT.setFill(kolor.get());
                Text lonT = new Text(x, anchorMapa.getHeight() - 8, String.format("%.4f°W", Math.abs(lon))); lonT.setFill(kolor.get());
                groupCarta.getChildren().addAll(latT, lonT);
            }
            case LINEA_ANGULO -> {
                double theta = Math.toRadians(transportadorG.getRotate());
                double dx = Math.cos(theta), dy = Math.sin(theta);
                double L = Math.max(imageCarta.getFitWidth(), imageCarta.getFitHeight()) * 2;
                Line l = new Line(x - dx * L, y - dy * L, x + dx * L, y + dy * L);
                styleShape(l);
                groupCarta.getChildren().add(l);
                addInteractiveBehaviour(l);
            }
            default -> {}
        }
    }

    /* ─────── Circunferencias ─────── */
    private void handlePress(double x, double y) {
        if (draggingNow) return;
        if (currentTool.get() != HerramientaActiva.CIRCUNFERENCIA) return;
        if (lastRadiusPx > 0) {
            Circle c = new Circle(x, y, lastRadiusPx);
            styleShape(c);
            c.setFill(Color.TRANSPARENT);
            double nm = lastDistanceNm.get();
            Text t = new Text(x, y - lastRadiusPx - 5, String.format("r = %.2f NM", nm));
            t.setFill(kolor.get());
            Circle h = new Circle(5, Color.ORANGE); h.setStroke(Color.WHITE); h.setStrokeWidth(1);
            updateHandlePos(h, x + lastRadiusPx, y);
            h.setCursor(Cursor.E_RESIZE);
            h.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> draggingNow = true);
            h.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
                Point2D p = groupCarta.sceneToLocal(e.getSceneX(), e.getSceneY());
                double r = Math.hypot(p.getX() - c.getCenterX(), p.getY() - c.getCenterY());
                c.setRadius(r);
                updateHandlePos(h, c.getCenterX() + r, c.getCenterY());
                double nm2 = r * mpxX;
                lastDistanceNm.set(nm2);
                t.setText(String.format("r = %.2f NM", nm2));
                t.setX(c.getCenterX());
                t.setY(c.getCenterY() - r - 5);
                e.consume();
            });
            h.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> { draggingNow = false; e.consume(); });
            Group circGroup = new Group(c, t, h);
            groupCarta.getChildren().add(circGroup);
            addInteractiveBehaviour(circGroup);
            return;
        }
        centroArcoX = x;
        centroArcoY = y;
        arcoEnCurso = new Arc(x, y, 0, 0, 0, 0);
        arcoEnCurso.setType(ArcType.OPEN);
        styleShape(arcoEnCurso);
        arcoEnCurso.setFill(Color.TRANSPARENT);
        groupCarta.getChildren().add(arcoEnCurso);
    }
    private void handleDrag(double x, double y) {
        if (currentTool.get() != HerramientaActiva.CIRCUNFERENCIA || arcoEnCurso == null) return;
        double r = Math.hypot(x - centroArcoX, y - centroArcoY);
        arcoEnCurso.setRadiusX(r);
        arcoEnCurso.setRadiusY(r);
        double sweep = Math.toDegrees(Math.atan2(y - centroArcoY, x - centroArcoX));
        arcoEnCurso.setStartAngle(0);
        arcoEnCurso.setLength(sweep <= 0 ? sweep + 360 : sweep);
    }
    private void handleRelease() { if (arcoEnCurso != null) { addInteractiveBehaviour(arcoEnCurso); arcoEnCurso = null; } }

    /* ─────────── Arrastre genérico ─────────── */
    private void makeDraggable(Node n) {
        n.setOnMousePressed(ev -> {
            draggingNow = true;
            dragAnchor = groupCarta.sceneToLocal(ev.getSceneX(), ev.getSceneY());
            initTX = n.getTranslateX();
            initTY = n.getTranslateY();
            prevTX = initTX;
            prevTY = initTY;
            n.setCursor(Cursor.MOVE);
            ev.consume();
        });
        n.setOnMouseDragged(ev -> {
            Point2D l = groupCarta.sceneToLocal(ev.getSceneX(), ev.getSceneY());
            double newTX = initTX + (l.getX() - dragAnchor.getX());
            double newTY = initTY + (l.getY() - dragAnchor.getY());
            double dx = newTX - prevTX;
            double dy = newTY - prevTY;
            n.setTranslateX(newTX);
            n.setTranslateY(newTY);
            if (n == selectedNode) {
                for (Node h : activeHandles) {
                    if (h instanceof Circle c) {
                        c.setCenterX(c.getCenterX() + dx);
                        c.setCenterY(c.getCenterY() + dy);
                    } else if (h instanceof Rectangle r) {
                        r.setX(r.getX() + dx);
                        r.setY(r.getY() + dy);
                    }
                }
            }
            prevTX = newTX; prevTY = newTY;
            ev.consume();
        });
        n.setOnMouseReleased(ev -> { n.setCursor(Cursor.DEFAULT); draggingNow = false; ev.consume(); });
    }

    /* ─────────── Interactividad común ─────────── */
    private void addInteractiveBehaviour(Node n) {
        makeDraggable(n);
        addContextMenuBasic(n);
        n.setOnMouseClicked(e -> { if (e.getClickCount() == 1) { highlightSelection(n); e.consume(); }});
    }

    /* ─────────── Selección + handles ─────────── */
    private void highlightSelection(Node n) {
        if (selectedNode != null) selectedNode.setEffect(null);
        activeHandles.forEach(groupCarta.getChildren()::remove);
        activeHandles.clear();
        selectedNode = n; if (n == null) return;
        n.setEffect(new DropShadow(10, Color.DODGERBLUE));

        if (n instanceof Group g && g.getChildren().size() == 2 && g.getChildren().get(0) instanceof Line l && g.getChildren().get(1) instanceof Text tag) {
            // Ya están los handlers en el grupo
            return;
        }
        if (n instanceof Group g2 && g2.getChildren().size() == 2 && g2.getChildren().get(0) instanceof Circle c && g2.getChildren().get(1) instanceof Text t) {
            createRadiusHandle(c, t);
            return;
        }
        if (n instanceof Line l2) {
            createEndpointHandle(l2, true);
            createEndpointHandle(l2, false);
        } else if (n instanceof Circle c2) {
            createRadiusHandle(c2, null);
        } else if (n instanceof Text t2) {
            createTextHandle(t2);
        }
    }

    /* ---------- Handles dependientes ---------- */
    private void createDistanceEndpointHandle(Line l, Text tag, boolean start) {
        Circle h = new Circle(5, Color.ORANGE); h.setStroke(Color.WHITE); h.setStrokeWidth(1);
        updateHandlePos(h, start ? l.getStartX() : l.getEndX(), start ? l.getStartY() : l.getEndY()); h.setCursor(Cursor.MOVE);
        h.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> draggingNow = true);
        h.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            Point2D p = groupCarta.sceneToLocal(e.getSceneX(), e.getSceneY());
            if (start) { l.setStartX(p.getX()); l.setStartY(p.getY()); } else { l.setEndX(p.getX()); l.setEndY(p.getY()); }
            double dx = l.getEndX() - l.getStartX(); double dy = l.getEndY() - l.getStartY(); double nm = Math.hypot(dx, dy) * mpxX;
            tag.setText(String.format("%.2f NM", nm)); tag.setX(l.getStartX() + dx / 2); tag.setY(l.getStartY() + dy / 2 - 5); lastDistanceNm.set(nm);
            updateHandlePos(h, p.getX(), p.getY()); e.consume();
        });
        h.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> { draggingNow = false; e.consume(); });
        addHandle(h);
    }
    private void createRadiusHandle(Circle c, Text tag) {
        Circle h = new Circle(5, Color.ORANGE); h.setStroke(Color.WHITE); h.setStrokeWidth(1);
        updateHandlePos(h, c.getCenterX() + c.getRadius(), c.getCenterY()); h.setCursor(Cursor.E_RESIZE);
        h.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> draggingNow = true);
        h.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            Point2D p = groupCarta.sceneToLocal(e.getSceneX(), e.getSceneY()); double r = Math.hypot(p.getX() - c.getCenterX(), p.getY() - c.getCenterY());
            c.setRadius(r); updateHandlePos(h, c.getCenterX() + r, c.getCenterY()); double nm = r * mpxX; lastDistanceNm.set(nm);
            if (tag != null) { tag.setText(String.format("r = %.2f NM", nm)); tag.setX(c.getCenterX()); tag.setY(c.getCenterY() - r - 5); } e.consume();
        });
        h.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> { draggingNow = false; e.consume(); });
        addHandle(h);
    }
    private void createEndpointHandle(Line l, boolean start) {
        Circle h = new Circle(5, Color.ORANGE); h.setStroke(Color.WHITE); h.setStrokeWidth(1);
        updateHandlePos(h, start ? l.getStartX() : l.getEndX(), start ? l.getStartY() : l.getEndY()); h.setCursor(Cursor.MOVE);
        h.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> draggingNow = true);
        h.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            Point2D p = groupCarta.sceneToLocal(e.getSceneX(), e.getSceneY()); if (start) { l.setStartX(p.getX()); l.setStartY(p.getY()); } else { l.setEndX(p.getX()); l.setEndY(p.getY()); }
            updateHandlePos(h, p.getX(), p.getY()); e.consume();
        });
        h.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> { draggingNow = false; e.consume(); });
        addHandle(h);
    }
    private void createRadiusHandle(Circle c) { createRadiusHandle(c, null); }
    private void createTextHandle(Text t) {
        Rectangle h = new Rectangle(8, 8, Color.ORANGE); h.setStroke(Color.WHITE); h.setStrokeWidth(1);
        updateHandlePos(h, t.getX() + t.getLayoutBounds().getWidth(), t.getY() - t.getLayoutBounds().getHeight()); h.setCursor(Cursor.MOVE);
        h.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> { dragAnchor = groupCarta.sceneToLocal(e.getSceneX(), e.getSceneY()); draggingNow = true; e.consume(); });
        h.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            Point2D p = groupCarta.sceneToLocal(e.getSceneX(), e.getSceneY()); double dx = p.getX() - dragAnchor.getX(), dy = p.getY() - dragAnchor.getY();
            t.setX(t.getX() + dx); t.setY(t.getY() + dy); updateHandlePos(h, h.getX() + dx, h.getY() + dy); dragAnchor = p; e.consume();
        });
        h.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> { draggingNow = false; e.consume(); });
        addHandle(h);
    }

    /* ---------- util ---------- */
    private void addHandle(Node h) { activeHandles.add(h); groupCarta.getChildren().add(h); h.toFront(); }
    private void updateHandlePos(Node h, double x, double y) {
        if (h instanceof Circle c) { c.setCenterX(x); c.setCenterY(y); } else if (h instanceof Rectangle r) { r.setX(x - 4); r.setY(y - 4); }
    }

    private void styleShape(Shape s) { s.setStroke(kolor.get()); s.setStrokeWidth(strokeWidth.get()); }
    private void addContextMenuBasic(Node n) {
        MenuItem del = new MenuItem("Eliminar"); del.setOnAction(e -> groupCarta.getChildren().remove(n));
        MenuItem col = new MenuItem("Cambiar color"); col.setOnAction(e -> { if (n instanceof Shape s) s.setStroke(kolor.get()); if (n instanceof Text t) t.setFill(kolor.get()); });
        ContextMenu m = new ContextMenu(col, del);
        n.setOnContextMenuRequested(e -> m.show(n, e.getScreenX(), e.getScreenY()));
    }

    /* ---------- API pública ---------- */
    public void setReglaVisible(boolean v) {
        reglaG.setVisible(v); if (!v) return; double cx = imageCarta.getFitWidth() / 2, cy = imageCarta.getFitHeight() / 2;
        reglaG.setTranslateX(cx - reglaG.getBoundsInParent().getWidth() / 2); reglaG.setTranslateY(cy - reglaG.getBoundsInParent().getHeight() / 2); reglaG.toFront();
    }
    public void setTransportadorVisible(boolean v) { transportadorG.setVisible(v); if (v) transportadorG.toFront(); }
    public void setCurrentTool(HerramientaActiva h) { currentTool.set(h); }
    public ObjectProperty<HerramientaActiva> currentToolProperty() { return currentTool; }
    public DoubleProperty strokeWidthProperty() { return strokeWidth; }

    /* ---------- limpiar ---------- */
    public void limpiar() {
        lastRadiusPx = -1; lastDistanceNm.set(0);
        groupCarta.getChildren().removeIf(n -> n instanceof Circle || n instanceof Line || n instanceof Arc || n instanceof Text || (n instanceof Group g && g != reglaG && g != transportadorG));
        highlightSelection(null);
    }
}