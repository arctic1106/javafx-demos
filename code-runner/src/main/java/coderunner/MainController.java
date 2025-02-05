package coderunner;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.fxml.Initializable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;

public class MainController implements Initializable {
    @FXML
    private TextField javaFileField;
    @FXML
    private Button browseButton;
    @FXML
    private TextField argsField;
    @FXML
    private Button runButton;
    @FXML
    private TextArea outputArea;
    @FXML
    private TextArea codeArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button saveAsButton;
    @FXML
    private Label statusBarLabel;

    public boolean isRunButtonDisabled() {
        return runButton.isDisabled();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        outputArea.setStyle("-fx-font-family: 'Courier New', monospace;");
        runButton.setDisable(true);
        saveButton.setDisable(true);
        saveAsButton.setDisable(true);
        javaFileField.textProperty()
                .addListener((_, _, newValue) -> runButton.setDisable(newValue == null || newValue.trim().isEmpty()));
        autoSelectFirstFile();
        Platform.runLater(this::setupKeyboardShortcuts);
        updateStatus("Ready");
    }

    @FXML
    public void handleRun() {
        disableAllButtons();
        var javaFile = javaFileField.getText();
        if (javaFile == null || javaFile.trim().isEmpty()) {
            outputArea.setText("Please select a Java file first.");
            updateStatus("Error: No Java file selected.");
            enableAllButtons();
            return;
        }
        outputArea.clear();
        outputArea.appendText("Running: " + javaFile + "\n");
        outputArea.appendText("=".repeat(20) + "\n\n");
        updateStatus("Running " + new File(javaFile).getName() + "...");

        var args = argsField.getText();
        if (args != null && !args.trim().isEmpty())
            outputArea.appendText("Arguments: " + args + "\n");

        var command = new ArrayList<>(List.of("java", javaFile));
        if (args != null && !args.trim().isEmpty())
            command.addAll(Arrays.asList(args.trim().split("\\s+")));

        var task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                var processBuilder = new ProcessBuilder(command);
                var process = processBuilder.start();
                var streamReaderExecutor = Executors.newVirtualThreadPerTaskExecutor();
                var stdoutRead = CompletableFuture.runAsync(() -> {
                    try {
                        readStream(process.getInputStream(), false);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                }, streamReaderExecutor);
                var stderrRead = CompletableFuture.runAsync(() -> {
                    try {
                        readStream(process.getErrorStream(), true);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                }, streamReaderExecutor);

                CompletableFuture.allOf(stdoutRead, stderrRead).join();
                int exitCode = process.waitFor();
                Platform.runLater(() -> {
                    outputArea.appendText("\n" + "=".repeat(20) + "\n");
                    outputArea.appendText("Process completed with exit code: " + exitCode + "\n");
                    updateStatus("Process finished with exit code: " + exitCode);
                });
                streamReaderExecutor.shutdown();
                return null;
            }
        };

        task.setOnFailed(_ -> Platform.runLater(() -> {
            var e = task.getException();
            outputArea.appendText("\nError executing command: " + e.getMessage() + "\n");
            updateStatus("Execution error: " + e.getMessage());
            enableAllButtons();
        }));

        task.setOnSucceeded(_ -> Platform.runLater(this::enableAllButtons));
        task.setOnCancelled(_ -> Platform.runLater(this::enableAllButtons));

        Executors.newVirtualThreadPerTaskExecutor().execute(task);
    }

    private void autoSelectFirstFile() {
        try {
            Files.walk(Paths.get(System.getProperty("user.dir"), "scripts"))
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".java"))
                    .findFirst()
                    .map(Path::toFile)
                    .ifPresentOrElse(
                            this::loadSelectedFile,
                            () -> updateStatus("No Java files found for auto-selection."));
        } catch (IOException e) {
            updateStatus("Error walking file tree: " + e.getMessage());
        }
    }

    @FXML
    public void handleBrowse() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Select Java File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Files", "*.java"));

        var currentDirPath = Paths.get(System.getProperty("user.dir"), "scripts");
        if (Files.exists(currentDirPath) && Files.isDirectory(currentDirPath)) {
            fileChooser.setInitialDirectory(currentDirPath.toFile());
        }

        var stage = (Stage) browseButton.getScene().getWindow();
        var selectedFile = fileChooser.showOpenDialog(stage);
        loadSelectedFile(selectedFile);
    }

    private void loadSelectedFile(File selectedFile) {
        if (selectedFile != null) {
            javaFileField.setText(selectedFile.getAbsolutePath());
            try {
                codeArea.clear();
                Files.readAllLines(selectedFile.toPath()).forEach(line -> codeArea.appendText(line + "\n"));
                saveButton.setDisable(false);
                saveAsButton.setDisable(false);
                updateStatus("File loaded: " + selectedFile.getName());
            } catch (IOException e) {
                outputArea.appendText("Error loading file: " + e.getMessage() + "\n");
                updateStatus("Error loading file: " + e.getMessage());
                saveButton.setDisable(true);
                saveAsButton.setDisable(true);
            }
            outputArea.clear();
        } else {
            saveButton.setDisable(true);
            saveAsButton.setDisable(true);
            updateStatus("File selection cancelled.");
        }
    }

    @FXML
    public void handleSave() {
        var currentFilePath = javaFileField.getText();
        if (currentFilePath == null || currentFilePath.trim().isEmpty()) {
            outputArea.appendText("No file selected to save. Use 'Save As' to create a new file.\n");
            updateStatus("Error: No file selected to save.");
            return;
        }

        var fileToSave = new File(currentFilePath);
        if (!fileToSave.exists()) {
            outputArea.appendText("File does not exist. Use 'Save As' to create a new file.\n");
            updateStatus("Error: File does not exist. Use 'Save As'.");
            return;
        }

        try {
            Files.writeString(fileToSave.toPath(), codeArea.getText());
            outputArea.appendText("File overwritten successfully: " + fileToSave.getAbsolutePath() + "\n");
            updateStatus("File saved: " + fileToSave.getName());
        } catch (IOException e) {
            outputArea.appendText("Error overwriting file: " + e.getMessage() + "\n");
            updateStatus("Error saving file: " + e.getMessage());
        }
    }

    @FXML
    public void handleSaveAs() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Save Java File As...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Files", "*.java"));

        var currentFilePath = javaFileField.getText();
        if (currentFilePath != null && !currentFilePath.trim().isEmpty()) {
            var currentFile = new File(currentFilePath);
            fileChooser.setInitialDirectory(currentFile.getParentFile());
            fileChooser.setInitialFileName(currentFile.getName());
        } else {
            var currentDir = new File(System.getProperty("user.dir"));
            if (currentDir.exists() && currentDir.isDirectory())
                fileChooser.setInitialDirectory(currentDir);
        }

        var stage = (Stage) saveAsButton.getScene().getWindow();
        var selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            try {
                Files.writeString(selectedFile.toPath(), codeArea.getText());
                Platform.runLater(() -> {
                    outputArea.appendText("File saved successfully to: " + selectedFile.getAbsolutePath() + "\n");
                    javaFileField.setText(selectedFile.getAbsolutePath());
                    saveButton.setDisable(false);
                    saveAsButton.setDisable(false);
                    updateStatus("File saved as: " + selectedFile.getName());
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    outputArea.appendText("Error saving file: " + e.getMessage() + "\n");
                    updateStatus("Error saving file as: " + e.getMessage());
                });
            }
        } else {
            updateStatus("Save As cancelled.");
        }
    }

    private void disableAllButtons() {
        runButton.setDisable(true);
        browseButton.setDisable(true);
        saveButton.setDisable(true);
        saveAsButton.setDisable(true);
        javaFileField.setDisable(true);
        argsField.setDisable(true);
    }

    private void enableAllButtons() {
        boolean isFileFieldEmpty = javaFileField.getText() == null || javaFileField.getText().trim().isEmpty();
        runButton.setDisable(isFileFieldEmpty);
        browseButton.setDisable(false);
        saveButton.setDisable(isFileFieldEmpty);
        saveAsButton.setDisable(isFileFieldEmpty);
        javaFileField.setDisable(false);
        argsField.setDisable(false);
        updateStatus("Ready");
    }

    private void readStream(java.io.InputStream inputStream, boolean isError) throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines().forEach(
                    line -> Platform.runLater(() -> outputArea.appendText((isError ? "[ERR] " : "") + line + "\n")));
        }
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> statusBarLabel.setText(message));
    }

    private void setupKeyboardShortcuts() {
        argsField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !runButton.isDisabled()) {
                handleRun();
                event.consume();
            }
        });

        var scene = browseButton.getScene();
        if (scene != null) {
            scene.setOnKeyPressed(event -> {
                if (event.isControlDown() && event.getCode() == KeyCode.O) {
                    handleBrowse();
                    event.consume();
                } else if (event.isControlDown() && event.getCode() == KeyCode.R && !runButton.isDisabled()) {
                    handleRun();
                    event.consume();
                } else if (event.isControlDown() && event.getCode() == KeyCode.S && !saveButton.isDisabled()) {
                    handleSave();
                    event.consume();
                } else if (event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.S
                        && !saveAsButton.isDisabled()) {
                    handleSaveAs();
                    event.consume();
                }
            });
        }
    }
}
