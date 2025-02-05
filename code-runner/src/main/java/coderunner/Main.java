package coderunner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        var loader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
        Parent root = loader.load();
        var scene = new Scene(root, 1200, 700);
        var css = getClass().getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        MainController controller = loader.getController();
        setupGlobalKeyboardShortcuts(scene, controller);

        stage.setTitle("Java File Runner");
        stage.setScene(scene);
        stage.setMinWidth(1200);
        stage.setMinHeight(700);
        stage.setResizable(true);
        stage.show();
    }

    private void setupGlobalKeyboardShortcuts(Scene scene, MainController controller) {
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
                controller::handleBrowse);

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
                () -> {
                    if (!controller.isRunButtonDisabled()) {
                        controller.handleRun();
                    }
                });

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.ENTER),
                () -> {
                    if (!controller.isRunButtonDisabled()) {
                        controller.handleRun();
                    }
                });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
