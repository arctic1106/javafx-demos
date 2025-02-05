package fxstarter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class View1 implements View {
    private final VBox view;

    public View1(Controller controller) {
        var label = new Label("View number 1");
        var button = new Button("Switch to View 2");
        button.setOnAction(e -> controller.switchToSecondary());

        view = new VBox(4, label, button);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(5));
    }

    @Override
    public Parent getParent() {
        return view;
    }
}
