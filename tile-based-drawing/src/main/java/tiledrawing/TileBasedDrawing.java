package tiledrawing;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TileBasedDrawing extends Application {

    private static final int TILESIZE = 20;
    private static final int[][] map = new int[40][40];
    private final File mapFile = new File("map.txt");
    private boolean isDragging = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        loadMap();

        stage.setTitle("Pixel Drawer");
        var canvas = new Canvas(800, 800);
        var graphicsContext = canvas.getGraphicsContext2D();
        canvas.setOnMousePressed(e -> handleMousePressed(e, graphicsContext));
        canvas.setOnMouseDragged(e -> handleMouseDragged(e, graphicsContext));
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnMouseClicked(e -> handleMouseClick(e, graphicsContext));
        var rootPane = new Pane(canvas);
        rootPane.setBackground(Background.fill(Color.BLACK));
        var scene = new Scene(rootPane, 800, 800);
        stage.setScene(scene);

        drawGrid(graphicsContext);
        drawMap(graphicsContext);

        stage.setOnCloseRequest(ignored -> saveMap());

        stage.show();
    }

    private void saveMap() {
        try (PrintWriter writer = new PrintWriter(mapFile)) {
            for (var x = 0; x < 40; x++) {
                for (var y = 0; y < 40; y++) {
                    writer.print(map[x][y]);
                    if (y < 39)
                        writer.print(" ");
                }
                writer.println();
            }
        } catch (IOException ignored) {
        }
    }

    private void loadMap() {
        if (mapFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(mapFile))) {
                for (var x = 0; x < 40; x++) {
                    String[] values = reader.readLine().split(" ");
                    for (var y = 0; y < 40; y++) {
                        map[x][y] = Integer.parseInt(values[y]);
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    private void drawMap(GraphicsContext graphicsContext) {
        for (var x = 0; x < 40; x++) {
            for (var y = 0; y < 40; y++) {
                drawTile(x, y, graphicsContext);
            }
        }
    }

    private void drawTile(int x, int y, GraphicsContext graphicsContext) {
        graphicsContext.setFill(map[x][y] == 1 ? Color.RED : Color.BLACK);
        graphicsContext.fillRect(x * TILESIZE, y * TILESIZE, TILESIZE, TILESIZE);
    }

    private void drawGrid(GraphicsContext graphicsContext) {
        graphicsContext.setFill(Color.LIGHTSLATEGRAY);
        for (var x = 0; x < 800; x += TILESIZE) {
            graphicsContext.strokeLine(x, 0, x, 800);
        }
        for (var y = 0; y < 800; y += TILESIZE) {
            graphicsContext.strokeLine(0, y, 800, y);
        }
    }

    private void handleMousePressed(MouseEvent event, GraphicsContext graphicsContext) {
        isDragging = true;
        handleMouseDragged(event, graphicsContext);
    }

    private void handleMouseDragged(MouseEvent event, GraphicsContext graphicsContext) {
        if (isDragging) {
            var x = event.getX();
            var y = event.getY();

            var tileX = (int) (x / TILESIZE);
            var tileY = (int) (y / TILESIZE);

            if (tileX >= 0 && tileX < 40 && tileY >= 0 && tileY < 40) {
                map[tileX][tileY] = 1;
                drawTile(tileX, tileY, graphicsContext);
            }
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        isDragging = false;
    }

    private void handleMouseClick(MouseEvent event, GraphicsContext graphicsContext) {
        var x = event.getX();
        var y = event.getY();
        var tileX = (int) (x / TILESIZE);
        var tileY = (int) (y / TILESIZE);
        if (tileX >= 0 && tileX < 40 && tileY >= 0 && tileY < 40) {
            map[tileX][tileY] = 1;
            drawTile(tileX, tileY, graphicsContext);
        }
    }
}
