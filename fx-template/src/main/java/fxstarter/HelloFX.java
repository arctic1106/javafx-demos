package fxstarter;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HelloFX extends Application implements DynamicViewHandler {
    private final VBox mainLayout = new VBox();

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("JavaFX Starter");
        createMainLayout();
        mainLayout.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(mainLayout, 400, 400));
        stage.show();
    }

    private void createMainLayout() {
        var dukeImage = new ImageView(new Image(getClass().getResourceAsStream("openduke.png")));
        dukeImage.setFitHeight(200);
        dukeImage.setPreserveRatio(true);

        var jfxVersion = new RedLabel("JavaFX version: " + System.getProperty("javafx.version"));
        var jdkVersion = new RedLabel("Java version: " + System.getProperty("java.version"));
        var dynamicView = new View1(new Controller(this));

        mainLayout.getChildren().addAll(dukeImage, jfxVersion, jdkVersion, dynamicView.getParent());
    }

    @Override
    public void setDynamicView(View newView) {
        mainLayout.getChildren().set(mainLayout.getChildren().size() - 1, newView.getParent());
    }
}
