import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * GUI pro aplikaci.
 */
public class MyWindow extends Application {
    /** Plátno */
    private final Canvas canvas;

    /** Plátno pro kreslení */
    private final GraphicsContext graphicsContext;

    /** Pole pro výsledek */
    private Text result;

    /** Pole pro nastavení velikosti pera */
    private TextField sWidth;

    /**
     * Konstruktor pro vytvoření třídy.
     */
    public MyWindow() {
        this.canvas = new Canvas(560, 560);
        this.graphicsContext = canvas.getGraphicsContext2D();
    }

    /**
     * Hlavní metoda pro proces tvorby okna.
     * @param primaryStage kontejner obsahující všechny prvky okna.
     */
    @Override
    public void start(Stage primaryStage) {
        initDraw(graphicsContext);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> {
                    graphicsContext.beginPath();
                    graphicsContext.moveTo(event.getX(), event.getY());
                    graphicsContext.stroke();
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> {
                    graphicsContext.lineTo(event.getX(), event.getY());
                    graphicsContext.stroke();
                });


        BorderPane rootPaneBP = new BorderPane();

        rootPaneBP.setCenter(canvas);
        rootPaneBP.setBottom(controlls());

        Scene scene = new Scene(rootPaneBP, 700, 700);
        primaryStage.setTitle("SP - KIV/UIR");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Vytvoří tlačítka.
     * @return komponenty.
     */
    private Node controlls() {
        GridPane controlPaneGP = new GridPane();
        controlPaneGP.setHgap(10);
        controlPaneGP.setVgap(10);
        controlPaneGP.setPadding(new Insets(10));

        Text description = new Text("Result:");
        controlPaneGP.add(description, 4, 0);

        Button button01 = new Button("Classify");
        controlPaneGP.add(button01, 0, 0);
        button01.setPrefSize(70, 35);
        button01.setOnAction(actionEvent -> process());

        Button clear = new Button("Clear");
        controlPaneGP.add(clear, 1, 0);
        clear.setPrefSize(70, 35);
        clear.setOnAction(actionEvent -> clearCanvas());

        sWidth = new TextField("50");
        controlPaneGP.add(sWidth, 3, 0);

        Button setW = new Button("Set width");
        setW.setPrefSize(70, 35);
        controlPaneGP.add(setW, 2, 0);
        setW.setOnAction(actionEvent -> setStroke());

        result = new Text("NaN");
        controlPaneGP.add(result, 5, 0);

        controlPaneGP.setAlignment(Pos.CENTER);
        return controlPaneGP;
    }

    /**
     * Nastaví velikost pera.
     */
    private void setStroke() {
        String width = sWidth.getText();

        try {
            graphicsContext.setLineWidth(Integer.parseInt(width));
        } catch (Exception e) {
            sWidth.setText("Wrong input");
        }
    }

    /**
     * Obnoví plátno.
     */
    private void clearCanvas() {
        graphicsContext.fillRect(0,0, canvas.getWidth(), canvas.getHeight());
        result.setText("NaN");
    }

    /**
     * Uloží plátno a vypíše výsledek.
     */
    private void process() {
        saveImg();
        int[] v = Main.getVector("numInput.png");
        int result;
        int len = 1;

        if (Main.param.equals(Main.hisRow)) {
            len = 28;
        }

        if (Main.clsfr.equals(Main.minDis)) {
            result = Main.minDis(Main.clases, v, len);
        } else {
            result = Main.knn(Main.entities, v);
        }

        this.result.setText(String.valueOf(result));
    }

    /**
     * Uloží plátno jako png obrázek.
     */
    private void saveImg() {
        String imgName = "numInput.png";

        WritableImage wim = new WritableImage(560, 560);
        canvas.snapshot(null, wim);

        BufferedImage img = SwingFXUtils.fromFXImage(wim, null);
        BufferedImage scaledImg = null;

        try {
            scaledImg = resizeImage(img,28,28);
        } catch (IOException e) {
            System.out.println("Failed to resize image.");
        }

        try {
            File outputfile = new File(imgName);
            ImageIO.write(scaledImg, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Změní velikost obrázku.
     * @param originalImage vstupní obrázek.
     * @param targetWidth šířka nového obrázku.
     * @param targetHeight výška nového obrázku.
     * @return nový obrázek.
     * @throws IOException nepodařilo se změnit obrázek.
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    /**
     * Nastaví kreslící plátno.
     * @param gc plátno pro kreslení.
     */
    private void initDraw(GraphicsContext gc){
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();

        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,canvasWidth,canvasHeight);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(50);
    }

    /**
     * Otevře okno.
     */
    public void open() {
        launch();
    }
}
